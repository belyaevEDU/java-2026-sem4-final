package org.example.apiInteraction;

import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class ApiHandlers {
    private final ApiRecord[] apis;

    public ApiHandlers() {
        ApiRecord[] apis1;
        ObjectMapper objectMapper = new ObjectMapper();
        apis1 = objectMapper.readValue(new File("src/main/resources/apis.json"), ApiRecord[].class);
        apis1 = Arrays.stream(apis1).sorted(Comparator.comparingInt(ApiRecord::id)).toArray(ApiRecord[]::new);
        this.apis = apis1;
    }

    protected ApiRecord[] getApis() {
        return this.apis;
    }

    @ApiHandler(apiId = 0)
    public HttpResponse<String> getMockAddressesResponse() {
        int apiId = 0;
        ApiRecord record = this.apis[0];
        if (record.id() != 0) {
            //
        }

        final String optionalParameters = "?_quantity=10";

        HttpResponse<String> response = null;

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(record.baseRequestURL() + optionalParameters))
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            //throw new RuntimeException(e);
        }

        return response;
    }
}
