package org.example.apiInteraction;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ApiHandler {
    private final ApiRecord apiRecord;

    public ApiHandler(ApiRecord apiRecord) {
        this.apiRecord = apiRecord;
    }

    public HttpResponse<String> getResponse() {
        String url = apiRecord.baseRequestURL();
        if (this.apiRecord.additionalPathNeeded()) {
            url += askUserForAdditionalPath();
        }

        HttpResponse<String> response = null;

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url));

            String baseAuthHeaderValue = "";
            final String authHeaderKey = "Authorization";

            if (this.apiRecord.headers() != null) {
                for (Map.Entry<String, String> header : this.apiRecord.headers().entrySet()) {
                    if (header.getKey().equals("Authorization")) { // warrants a rewrite cuz this is SHHHHIT
                        continue;
                    }
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }

            if (this.apiRecord.keyNeeded()) {
                if (this.apiRecord.headers() != null) {
                    if (this.apiRecord.headers().containsKey(authHeaderKey)) {
                        baseAuthHeaderValue = this.apiRecord.headers().get(authHeaderKey);
                    }
                }

                requestBuilder.header(authHeaderKey, baseAuthHeaderValue + this.apiRecord.apiKey());
            }

            HttpRequest request = requestBuilder.GET().build();


            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return response;
    }

    private String askUserForAdditionalPath() {
        String[] paths = this.apiRecord.additionalPaths();

        int index = 0;

        System.out.println("Pick a path: ");
        for (int i = 0; i < paths.length; i++) {
            System.out.println((i + 1) + ": " + paths[i]);
        }

        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        while (!done) {
            System.out.print("\nChoose: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("random")) {
                index = new Random().nextInt(paths.length);
                break;
            }

            try {
                index = Integer.parseInt(userInput);
                index--; // since the user input starts with 1
                if (index >= 0 && index <= paths.length) {
                    done = true;
                } else {
                    System.out.println("Error: out of bounds!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: not a number!");
            }
        }

        return paths[index];
    }
}
