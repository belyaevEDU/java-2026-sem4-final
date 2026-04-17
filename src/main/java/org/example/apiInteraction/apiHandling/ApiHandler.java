package org.example.apiInteraction.apiHandling;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public record ApiHandler(ApiRecord apiRecord) {
    public HttpResponse<String> getResponse(@Nullable String additionalPath) {
        String url = apiRecord.baseRequestURL();
        if (this.apiRecord.additionalPathNeeded()) {
            if (additionalPath == null) {
                throw new IllegalArgumentException("additional path needed " +
                        "and optional of additional path passed is empty");
            }
            url += additionalPath;
        }

        HttpResponse<String> response = null;

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url));

            String baseAuthHeaderValue = "";
            final String authHeaderKey = "Authorization";

            if (this.apiRecord.headers() != null) {
                for (Map.Entry<String, String> header : this.apiRecord.headers().entrySet()) {
                    if (header.getKey().equals(authHeaderKey)) {
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
}
