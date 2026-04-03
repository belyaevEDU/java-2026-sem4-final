package org.example.apiInteraction;

import org.example.apiInteraction.CliInteraction.InteractiveUtils;
import org.example.apiInteraction.CliInteraction.UserResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.*;

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
        final String apiSelectionAskMessage = "\nSelect by ID which APIs to call (\"all\" for all of them; can do several, ex.: 12): ";
        final String outputApiSelectAskMessage = "\nSelect by ID which API responses to print (\"all\" for all of them; can do several): ";
        final String defaultResultFileName = "output_%d.txt";

        boolean isInteractive = this.interactiveUtils.askUserWhetherInteractive();
        UserResponse userResponse;

        if (isInteractive) {
            userResponse = this.interactiveMode(apiSelectionAskMessage, outputApiSelectAskMessage);
        } else {
            userResponse = this.automaticMode(apiSelectionAskMessage);
        }

        for (int apiId : userResponse.idToFormatFile().keySet()) {
            ApiRecord api = null;
            for (ApiRecord thisApi : apis) {
                if (thisApi.id() == apiId) {
                    api = thisApi;
                }
            }

            if (api == null) {
                throw new IllegalArgumentException("Api not found by id");
            }

            ApiHandler handler = new ApiHandler(api);
            Optional<String> additionalPathOpt = Optional.empty();
            if (api.additionalPathNeeded() && isInteractive) {
                String additionalPath = this.interactiveUtils.askUserForAdditionalPath(api);
                additionalPathOpt = Optional.of(additionalPath);
            } else if (api.additionalPathNeeded()) { // is going to be automatic
                String[] possiblePaths = api.additionalPaths();
                if (possiblePaths.length == 0) {
                    throw new IllegalArgumentException("In API " + api.id() + " there's additional paths needed " +
                            "and no addition path specified");
                }

                int randomElement = new Random().nextInt(possiblePaths.length);
                String additionalPath = possiblePaths[randomElement];
                additionalPathOpt = Optional.of(additionalPath);
            }
            HttpResponse<String> response = handler.getResponse(additionalPathOpt);

            if (response == null) {
                System.out.println("ERROR: Response returned as null");
            } else {
                if (response.statusCode() != 200) {
                    System.out.println("ERROR: Response code non-200 - " + response.statusCode());
                } else {
                    OutputFormatter outputFormatting = new OutputFormatter(BASE_FILE_PATH +
                            userResponse.idToFormatFile().get(apiId), response.body());
                    String result = outputFormatting.format();

                    FileHandler.writeToFile(result, userResponse.toAppend(),
                            BASE_FILE_PATH + String.format(defaultResultFileName, apiId));

                    if (Arrays.stream(userResponse.outputApiIDs()).anyMatch(e -> e == apiId)) {
                        System.out.println("Api " + api.name() + ": ");
                        System.out.println(result);
                    }
                }
            }
        }
    }

    private UserResponse interactiveMode(String apiSelectionAskMessage, String outputApiSelectAskMessage) {
        int[] apiIDs = this.interactiveUtils.askUserForApis(apiSelectionAskMessage);

        Map<Integer, String> idToFormattingFile = this.interactiveUtils.getIdsToFormattingFileFromUser(apiIDs);

        boolean appendToFile = this.interactiveUtils.askUserWhetherToAppend(); // if 1 append, if 0 re-write
        int[] outputApiIDs = this.interactiveUtils.askUserForApis(outputApiSelectAskMessage);

        return new UserResponse(idToFormattingFile, appendToFile, outputApiIDs);
    }

    private UserResponse automaticMode(String apiSelectionAskMessage) {
        int[] apiIDs = this.interactiveUtils.askUserForApis(apiSelectionAskMessage);

        Map<Integer, String> idToFormattingFile = this.interactiveUtils.getIdsToFormattingFileFromUser(apiIDs);

        boolean appendToFile = false; // always rewrite
        int[] cliOutputApiIDs = new int[]{}; // always empty

        return new UserResponse(idToFormattingFile, appendToFile, cliOutputApiIDs);
    }
}
