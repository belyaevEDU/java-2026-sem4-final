package org.example.apiInteraction;

import tools.jackson.databind.ObjectMapper;

import java.io.File;

public class ApiInteraction {
    private final ApiRecord[] apis;

    public ApiInteraction() {
        ObjectMapper objectMapper = new ObjectMapper();
        apis = objectMapper.readValue(new File("src/main/resources/apis.json"), ApiRecord[].class);
    }
}
