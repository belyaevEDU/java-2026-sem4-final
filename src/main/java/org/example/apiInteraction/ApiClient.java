package org.example.apiInteraction;

import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.http.HttpResponse;

public class ApiInteraction {
    private final ApiRecord[] apis;
    private final String BASE_FILE_PATH = "src/main/resources/";
    private final String FILE_PATH = BASE_FILE_PATH + "apis.json";

    public ApiInteraction() {
        ApiRecord[] apisTemp;
        ObjectMapper objectMapper = new ObjectMapper();
        apisTemp = objectMapper.readValue(new File(this.FILE_PATH), ApiRecord[].class);
        this.apis = apisTemp;
    }

    public void interact() {


        for (ApiRecord api : apis) {
            System.out.println("Api " + (api.id() + 1) + ", " + api.name() + ":");

            ApiHandler handler = new ApiHandler(api);
            HttpResponse<String> response = handler.getResponse();

            if (response == null) {
                System.out.println("Response returned as null");
            } else {
                if (response.statusCode() != 200) {
                    System.out.println("Response code non-200: " + response.statusCode());
                } else {
                    OutputFormatting outputFormatting = new OutputFormatting(BASE_FILE_PATH + "outputFormat_" + api.id() + ".txt", response.body());
                    System.out.println(outputFormatting.format() + "\n");
                }
            }
        }
    }

    private void interactiveMode() {
        
    }
}
