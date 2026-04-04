package org.example;

import org.example.apiInteraction.UserInteraction;
import org.jetbrains.annotations.NotNull;

import org.example.apiInteraction.RunArgs.*;

import java.util.Locale;

public class Main {
    static void main(String @NotNull [] args) {
        // possible args:
        // type (optional)format

        // so all possible (rather viable) argument combinations:
        // interactive
        // automatic csv
        // automatic json

        if (args.length == 0) {
            throw new IllegalArgumentException("ERROR: Run arguments not specified.");
        }

        UserInteractionType userInteractionType;
        FileType fileType = null;

        if (args[0].equals(enumToString(UserInteractionType.INTERACTIVE)) && args.length == 1) {
            userInteractionType = UserInteractionType.INTERACTIVE;
        } else if (args[0].equals(enumToString(UserInteractionType.AUTOMATIC)) && args.length == 2) {
            userInteractionType = UserInteractionType.AUTOMATIC;

            if (args[1].equals(enumToString(FileType.CSV))) {
                fileType = FileType.CSV;
            } else if (args[1].equals(enumToString(FileType.JSON))) {
                fileType = FileType.JSON;
            } else {
                throw new IllegalArgumentException("ERROR: Illegal file type argument specified.");
            }
        } else {
            throw new IllegalArgumentException("ERROR: Illegal run-mode arguments specified.");
        }

        UserInteraction userInteraction = new UserInteraction();
        userInteraction.interact(userInteractionType, fileType);
    }

    static String enumToString(@NotNull Enum<?> enu) {
        return enu.name().toLowerCase(Locale.ROOT);
    }
}
