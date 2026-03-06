package org.example.apiIntegration;

import tools.jackson.databind.ObjectMapper;

import java.io.File;

public class ApiIntegration {
    private final ApiRecord[] apis;

    public ApiIntegration() {
        ObjectMapper objectMapper = new ObjectMapper();
        apis = objectMapper.readValue(new File("src/main/resources/apis.json"), ApiRecord[].class);
    }
}
