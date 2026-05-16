package org.example.apiInteraction;

import org.example.apiInteraction.apiHandling.ApiHandler;
import org.example.apiInteraction.apiHandling.ApiRecord;
import org.example.apiInteraction.resultFormatting.CustomFormatter;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class PollingManager {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Set<ApiRecord> apis;
    private final Map<Integer, String> additionalPaths;
    private final int maxConcurrent;
    private final long intervalSeconds;
    private final CustomFormatter formatter;
    private final FileHandler fileHandler;

    private final ReentrantLock writeLock = new ReentrantLock();
    private final Semaphore semaphore;
    private volatile String currentContent;

    private ScheduledExecutorService scheduler;
    private ExecutorService workerPool;

    public PollingManager(Set<ApiRecord> apis, Map<Integer, String> additionalPaths,
                          int maxConcurrent, long intervalSeconds,
                          CustomFormatter formatter, FileHandler fileHandler,
                          String initialContent) {
        this.apis = apis;
        this.additionalPaths = additionalPaths;
        this.maxConcurrent = maxConcurrent;
        this.intervalSeconds = intervalSeconds;
        this.formatter = formatter;
        this.fileHandler = fileHandler;
        this.currentContent = initialContent;
        this.semaphore = new Semaphore(maxConcurrent);
    }

    public void start() {
        scheduler = Executors.newScheduledThreadPool(apis.size());
        workerPool = Executors.newCachedThreadPool();

        for (ApiRecord api : apis) {
            if (intervalSeconds == 0) {
                submitContinuous(api);
            } else {
                scheduler.scheduleAtFixedRate(() -> workerPool.submit(() -> pollApi(api)),
                        0, intervalSeconds, TimeUnit.SECONDS
                );
            }
        }

        System.out.println("Started polling manager: apis=" + apis.size() + ", maxConcurrent=" + maxConcurrent
                + ", interval=" + intervalSeconds + "s.");
    }

    public void stop() {
        if (scheduler == null || scheduler.isShutdown()) return;

        System.out.println("Stopping polling manager...");
        scheduler.shutdown();
        workerPool.shutdown();
        try {
            if (!scheduler.awaitTermination(20, TimeUnit.SECONDS)) scheduler.shutdownNow();
            if (!workerPool.awaitTermination(20, TimeUnit.SECONDS)) workerPool.shutdownNow();
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Stopped polling manager.");
    }

    private void submitContinuous(ApiRecord api) {
        workerPool.submit(() -> {
            pollApi(api);
            if (!workerPool.isShutdown()) {
                submitContinuous(api);
            }
        });
    }

    private void pollApi(ApiRecord api) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            String additionalPath = additionalPaths.get(api.id());
            ApiHandler handler = new ApiHandler(api);
            HttpResponse<String> response = handler.getResponse(additionalPath);

            if (response == null) {
                System.out.println("<" + getCurrentTimestamp() + "> Null response from " + api.name());
                return;
            }
            if (response.statusCode() != 200) {
                System.out.println("<" + getCurrentTimestamp() + "> ERROR: Code " + response.statusCode() + " from " + api.name());
                return;
            }

            writeResult(api, response.body());
        } finally {
            semaphore.release();
        }
    }

    private void writeResult(ApiRecord api, String responseBody) {
        writeLock.lock();
        try {
            formatter.setSourceName(api.name());
            String formatted = formatter.format(responseBody, currentContent);
            fileHandler.write(formatted);
            currentContent = formatted;
            System.out.println("<" + getCurrentTimestamp() + "> Polled " + api.name());
        } catch (IOException e) {
            System.out.println("<" + getCurrentTimestamp() + "> Error writing result for " + api.name());
        } finally {
            writeLock.unlock();
        }
    }

    private static String getCurrentTimestamp() {
        return LocalTime.now().format(TIME_FORMAT);
    }
}
