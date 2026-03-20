package org.example.apiInteraction;

import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.http.HttpResponse;

public class ApiClient {
    private final ApiRecord[] apis;
    private final InteractiveUtils interactiveUtils;
    private final String BASE_FILE_PATH = "src/main/resources/";
    private final String FILE_PATH = BASE_FILE_PATH + "apis.json";

    public ApiClient() {
        ObjectMapper objectMapper = new ObjectMapper();
        this.apis = objectMapper.readValue(new File(this.FILE_PATH), ApiRecord[].class);

        interactiveUtils = new InteractiveUtils(this.apis);
    }

    public void call() {


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
                    OutputFormatter outputFormatting = new OutputFormatter(BASE_FILE_PATH + "outputFormat_" + api.id() + ".txt", response.body());
                    System.out.println(outputFormatting.format() + "\n");
                }
            }
        }
    }

    private void interactiveMode() {
        final String apiSelectionAskMessage = "\nSelect by ID which APIs to call (\"all\" for all of them; can do several, ex.: 12): ";
        final String outputApiSelectAskMessage = "\nSelect by ID which API responses to print (\"all\" for all of them; can do several): ";

        int[] apiIDs = this.interactiveUtils.askUserForApis(apiSelectionAskMessage);
        boolean appendToFile = this.interactiveUtils.askUserWhetherToAppend(); // if 1 append, if 0 re-write
        int[] outputApiIDs = this.interactiveUtils.askUserForApis(outputApiSelectAskMessage);

        // ask user for formatting file name. have "default" option


    }
}
