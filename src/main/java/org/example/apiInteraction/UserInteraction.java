package org.example.apiInteraction;

import org.example.apiInteraction.RunArgs.FileType;
import org.example.apiInteraction.RunArgs.UserInteractionType;
import org.example.apiInteraction.apiHandling.ApiRecord;
import org.example.apiInteraction.cliInteraction.InteractiveUtils;
import org.example.apiInteraction.cliInteraction.UserResponse;
import org.example.apiInteraction.cliInteraction.WriteMode;
import org.example.apiInteraction.resultFormatting.CSVResultFormatter;
import org.example.apiInteraction.resultFormatting.CustomFormatter;
import org.example.apiInteraction.resultFormatting.JSONResultFormatter;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserInteraction {
    private final ApiRecord[] apis;
    private final InteractiveUtils interactiveUtils;
    private final String BASE_FILE_PATH = "src/main/resources/";

    public UserInteraction() {
        ObjectMapper objectMapper = new ObjectMapper();
        String FILE_PATH = BASE_FILE_PATH + "apis.json";
        this.apis = objectMapper.readValue(new File(FILE_PATH), ApiRecord[].class);

        interactiveUtils = new InteractiveUtils(this.apis);
    }

    // Package-private constructor for unit tests. Bypasses file I/O.
    UserInteraction(ApiRecord[] apis, InteractiveUtils interactiveUtils) {
        this.apis = apis;
        this.interactiveUtils = interactiveUtils;
    }

    public void interact() {
        final String apiSelectionMsg = "\nSelect by ID which APIs to call " +
                "(\"all\" for all of them; can do several, ex.: 1,2,12): ";
        final String apiCliOutputMsg = "\nSelect by ID which APIs to output " +
                "(\"all\" for all of them; can do several, ex.: 1,2,12): ";

        int[] apiIDs = interactiveUtils.askUserForApis(apiSelectionMsg);
        int[] outputApiIDs = interactiveUtils.askUserForApis(apiCliOutputMsg, apiIDs);
        WriteMode writeMode = interactiveUtils.askUserWhetherToAppend();
        FileType fileType = interactiveUtils.askUserForFileType();
        int n = interactiveUtils.askUserForMaxConcurrent();
        int t = interactiveUtils.askUserForInterval();

        UserResponse userResponse = new UserResponse(apiIDs, writeMode, outputApiIDs);
        startPolling(userResponse, fileType, n, t, UserInteractionType.INTERACTIVE);
    }

    public void interact(@NotNull FileType fileType, int maxConcurrent, int intervalSeconds) {
        final String apiSelectionMsg = "\nSelect by ID which APIs to call " +
                "(\"all\" for all of them; can do several, ex.: 12): ";

        int[] apiIDs = interactiveUtils.askUserForApis(apiSelectionMsg);
        UserResponse userResponse = new UserResponse(apiIDs, WriteMode.OVERWRITE, new int[]{});
        startPolling(userResponse, fileType, maxConcurrent, intervalSeconds, UserInteractionType.AUTOMATIC);
    }


    private void startPolling(UserResponse userResponse, FileType fileType, int maxConcurrent,
                              int intervalSeconds, UserInteractionType type) {
        final String defaultFileNameFormat = "output.%s";

        Set<ApiRecord> selectedApis = resolveApis(userResponse.apiIdsToCall());
        Map<Integer, String> additionalPaths = resolveAdditionalPaths(selectedApis, type);

        String fileName = String.format(defaultFileNameFormat, fileType.toString().toLowerCase());
        FileHandler fileHandler = new FileHandler(BASE_FILE_PATH + fileName);
        CustomFormatter formatter = buildFormatter(fileType);

        String initialContent = "";
        if (userResponse.writeMode().equals(WriteMode.APPEND)) {
            try {
                initialContent = fileHandler.read();
            } catch (IOException e) {
                System.out.println("ERROR: couldn't read existing file " + e.getMessage());
                return;
            }
        }

        PollingManager pollingManager = new PollingManager(
                selectedApis, additionalPaths, maxConcurrent,
                intervalSeconds, formatter, fileHandler, initialContent
        );

        if (type.equals(UserInteractionType.INTERACTIVE)) {
            runInteractive(pollingManager);
        } else {
            runAutomatic(pollingManager);
        }
    }

    private void runInteractive(PollingManager pollingManager) {
        pollingManager.start();
        System.out.println("\nPolling is running. Type 'stop' and press Enter to stop.\n");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            if (scanner.nextLine().trim().equalsIgnoreCase("stop")) break;
            System.out.println("(Type 'stop' to halt polling)");
        }

        pollingManager.stop();
        System.out.println("Polling stopped. Results saved to file.");
    }

    private void runAutomatic(PollingManager pollingManager) {
        pollingManager.start();
        System.out.println("Automatic polling started. Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown] Stopping polling…");
            pollingManager.stop();
        }, "shutdown-hook"));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @NotNull Map<Integer, String> resolveAdditionalPaths(@NotNull Set<ApiRecord> selectedApis, UserInteractionType type) {
        Map<Integer, String> result = new HashMap<>();

        for (ApiRecord api : selectedApis) {
            if (!api.additionalPathNeeded()) continue;

            String[] options = api.additionalPaths();
            if (options == null || options.length == 0) {
                throw new IllegalArgumentException(
                        "ERROR: API '" + api.name() + "' needs an additional path "
                                + "but none are configured in apis.json.");
            }

            String chosen;
            if (type.equals(UserInteractionType.INTERACTIVE)) {
                chosen = interactiveUtils.askUserForAdditionalPath(api);
            } else {
                chosen = options[new Random().nextInt(options.length)];
            }

            result.put(api.id(), chosen);
        }

        return result;
    }

    @NotNull Set<ApiRecord> resolveApis(int @NotNull [] apiIds) {
        Set<ApiRecord> result = new HashSet<>();
        for (int id : apiIds) {
            ApiRecord found = null;
            for (ApiRecord api : apis) {
                if (api.id() == id) { found = api; break; }
            }
            if (found == null) {
                throw new IllegalArgumentException("ERROR: API not found for id=" + id);
            }
            result.add(found);
        }
        return result;
    }

    @NotNull CustomFormatter buildFormatter(@NotNull FileType fileType) {
        return switch (fileType) {
            case CSV -> new CSVResultFormatter("");
            case JSON -> new JSONResultFormatter("");
        };
    }
}
