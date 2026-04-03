package org.example;

import org.example.apiInteraction.RunArgs;
import org.example.apiInteraction.UserInteraction;
import org.jetbrains.annotations.NotNull;

public class Main {
    static void main(@NotNull String[] args) {
        // possible args:
        // type (optional)format

        // so all possible (rather viable) argument combinations:
        // interactive
        // automatic csv
        // automatic json

        final String interactiveArg = "interactive";
        final String automaticArg = "automatic";
        final String csvArg = "csv";
        final String jsonArg = "json";


        if (args.length == 0) {
            throw new IllegalArgumentException("ERROR: Run arguments not specified.");
        }

        RunArgs.UserInteractionType userInteractionType;
        RunArgs.FileType fileType = null;

        if (args[0].equals(interactiveArg) && args.length == 1) {
            userInteractionType = RunArgs.UserInteractionType.INTERACTIVE;
        } else if (args[0].equals(automaticArg) && args.length == 2) {
            userInteractionType = RunArgs.UserInteractionType.AUTOMATIC;

            if (args[1].equals(csvArg)) {
                fileType = RunArgs.FileType.CSV;
            } else if (args[1].equals(jsonArg)) {
                fileType = RunArgs.FileType.JSON;
            } else {
                throw new IllegalArgumentException("ERROR: Illegal file type argument specified.");
            }
        } else {
            throw new IllegalArgumentException("ERROR: Illegal run-mode arguments specified.");
        }

        UserInteraction userInteraction = new UserInteraction();
        userInteraction.interact(userInteractionType, fileType);
    }
}
