package org.example;

import org.example.apiInteraction.UserInteraction;
import org.jetbrains.annotations.NotNull;

import org.example.apiInteraction.RunArgs.*;

import java.util.Locale;

public class Main {
    public static void main(String @NotNull [] args) {
        // possible args:
        // type (optional)format

        // so all possible (rather viable) argument combinations:
        // interactive
        // automatic csv <max concurrent> <interval>
        // automatic json <max concurrent> <interval>

        if (args.length == 0) {
            throw new IllegalArgumentException("ERROR: Run arguments not specified.");
        }

        UserInteraction userInteraction = new UserInteraction();

        FileType fileType;

        if (args[0].equals(enumToString(UserInteractionType.INTERACTIVE)) && args.length == 1) {
            userInteraction.interact();
        } else if (args[0].equals(enumToString(UserInteractionType.AUTOMATIC)) && args.length == 4) {
            if (args[1].equals(enumToString(FileType.CSV))) {
                fileType = FileType.CSV;
            } else if (args[1].equals(enumToString(FileType.JSON))) {
                fileType = FileType.JSON;
            } else {
                throw new IllegalArgumentException("ERROR: Illegal file type argument specified.");
            }

            int maxConcurrent = validateNonNegativeInt(args[2]);
            int intervalSeconds = validateNonNegativeInt(args[3]);

            userInteraction.interact(fileType, maxConcurrent, intervalSeconds);
        } else {
            throw new IllegalArgumentException("ERROR: Illegal run-mode arguments specified.");
        }

    }

    static String enumToString(@NotNull Enum<?> enu) {
        return enu.name().toLowerCase(Locale.ROOT);
    }

    private static int validateNonNegativeInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value < 0) {
                throw new IllegalArgumentException("ERROR: integer arg parameter must be non-negative");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ERROR: arg parameter expected to be integer");
        }
    }
}
