package org.example.apiInteraction;

import org.example.apiInteraction.cliInteraction.InteractiveUtils;
import org.example.apiInteraction.cliInteraction.UserResponse;
import org.example.apiInteraction.cliInteraction.WriteMode;
import org.example.apiInteraction.apiHandling.ApiHandler;
import org.example.apiInteraction.apiHandling.ApiRecord;
import org.example.apiInteraction.resultFormatting.CSVResultFormatter;
import org.example.apiInteraction.resultFormatting.CustomFormatter;
import org.example.apiInteraction.resultFormatting.JSONResultFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

import org.example.apiInteraction.RunArgs.*;

public class UserInteraction {
    private final ApiRecord[] apis;
    private final InteractiveUtils interactiveUtils;
    private final String BASE_FILE_PATH = "src/main/resources/";
    private final String FILE_PATH = BASE_FILE_PATH + "apis.json";

    public UserInteraction() {
        ObjectMapper objectMapper = new ObjectMapper();
        this.apis = objectMapper.readValue(new File(this.FILE_PATH), ApiRecord[].class);

        interactiveUtils = new InteractiveUtils(this.apis);
    }

    public void interact(@NotNull UserInteractionType userInteractionType, @Nullable FileType fileType) {
        final String apiSelectionAskMessage = "\nSelect by ID which APIs to call (\"all\" for all of them; can do several, ex.: 12): ";
        final String outputApiSelectAskMessage = "\nSelect by ID which API responses to print (\"all\" for all of them; " +
                "can do several; \"none\" for none of them): ";
        final String defaultResultFileName = "output.%s";

        UserResponse userResponse;

        if (userInteractionType.equals(UserInteractionType.INTERACTIVE)) {
            userResponse = this.interactiveMode(apiSelectionAskMessage, outputApiSelectAskMessage);
            if (fileType == null) {
                fileType = this.interactiveUtils.askUserForFileType();
            }
        } else if (userInteractionType.equals(UserInteractionType.AUTOMATIC)) {
            userResponse = this.automaticMode(apiSelectionAskMessage);
        } else {
            throw new IllegalArgumentException("ERROR: Interaction type unrecognized");
        }

        assert fileType != null;
        String fileName = String.format(defaultResultFileName, fileType.toString().toLowerCase());
        final FileHandler resultFileHandler = new FileHandler(BASE_FILE_PATH + fileName);

        CustomFormatter formatter;
        if (fileType.equals(FileType.CSV)) {
            formatter = new CSVResultFormatter("");
        } else if (fileType.equals(FileType.JSON)) {
            formatter = new JSONResultFormatter("");
        } else {
            throw new IllegalArgumentException("ERROR: Unrecognized file type");
        }

        String currentContent = "";
        if (userResponse.writeMode().equals(WriteMode.APPEND)) {
            try {
                currentContent = resultFileHandler.read();
            } catch (IOException e) {
                System.out.println("ERROR reading existing file: " + e.getMessage());
            }
        }

        for (int apiId : userResponse.apiIdsToCall()) {
            ApiRecord api = null;
            for (ApiRecord thisApi : apis) {
                if (thisApi.id() == apiId) {
                    api = thisApi;
                }
            }

            if (api == null) {
                throw new IllegalArgumentException("ERROR: Api not found by id");
            }

            ApiHandler handler = new ApiHandler(api);
            String additionalPath = null;
            if (api.additionalPathNeeded() && userInteractionType.equals(UserInteractionType.INTERACTIVE)) {
                additionalPath = this.interactiveUtils.askUserForAdditionalPath(api);
            } else if (api.additionalPathNeeded()) { // it's going to be automatic (ide was giving me a warning)
                String[] possiblePaths = api.additionalPaths();
                if (possiblePaths.length == 0) {
                    throw new IllegalArgumentException("ERROR: In API " + api.id() + " there's additional paths needed " +
                            "and no additional path(s) specified");
                }

                int randomElement = new Random().nextInt(possiblePaths.length);
                additionalPath = possiblePaths[randomElement];
            }
            HttpResponse<String> response = handler.getResponse(additionalPath);

            if (response == null) {
                throw new IllegalArgumentException("ERROR: Response returned as null");
            }
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("ERROR: Response code non-200 - " + response.statusCode());
            }

            formatter.setSourceName(api.name());

            try {
                String formatted = formatter.format(response.body(), currentContent);
                resultFileHandler.write(formatted);
                currentContent = formatted;
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }

            // only interactive has cli result output
            if (Arrays.stream(userResponse.outputApiIDs()).anyMatch(e -> e == apiId)) {
                System.out.println("Api " + api.name() + ": ");
                System.out.println(response.body());
            }
        }
    }


    private UserResponse interactiveMode(String apiSelectionAskMessage, String outputApiSelectAskMessage) {
        int[] apiIDs = this.interactiveUtils.askUserForApis(apiSelectionAskMessage);

        WriteMode appendToFile = this.interactiveUtils.askUserWhetherToAppend(); // if 1 append, if 0 re-write
        int[] outputApiIDs = this.interactiveUtils.askUserForApis(outputApiSelectAskMessage, apiIDs);

        return new UserResponse(apiIDs, appendToFile, outputApiIDs);
    }

    private UserResponse automaticMode(String apiSelectionAskMessage) {
        int[] apiIDs = this.interactiveUtils.askUserForApis(apiSelectionAskMessage);

        WriteMode appendToFile = WriteMode.OVERWRITE; // always rewrite
        int[] cliOutputApiIDs = new int[]{}; // always empty

        return new UserResponse(apiIDs, appendToFile, cliOutputApiIDs);
    }
}
