package org.example.apiInteraction.CliInteraction;

import org.example.apiInteraction.ApiRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class InteractiveUtils {
    private final ApiRecord[] apis;
    private final String BASE_FILE_PATH = "src/main/resources/";

    public InteractiveUtils(ApiRecord[] apis) {
        this.apis = apis;
    }

    public int[] askUserForApis(String askMessage) {
        final String allApisUserResponse = "all";

        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        int[] array = new int[]{};

        while (!done) {
            System.out.println("APIs:");
            for (ApiRecord api : apis) {
                System.out.println(api);
            }

            System.out.print(askMessage);
            String userInput = scanner.nextLine();

            if (userInput.isEmpty()) {
                System.out.println("Error: user input is empty.\n");
                continue;
            }

            if (userInput.equals(allApisUserResponse)) {
                array = new int[this.apis.length];
                for (int i = 0; i < this.apis.length; i++) {
                    array[i] = i;
                }
                done = true;
                continue;
            }

            array = new int[userInput.length()];
            char[] charArray = userInput.toCharArray();
            try {
                for (int i = 0; i < userInput.length(); i++) {
                    array[i] = Integer.parseInt(String.valueOf(charArray[i]));
                }
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: non-numeric input.\n");
            }
        }

        return array;
    }

    public boolean askUserWhetherToAppend() {
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        boolean result = false;

        while (!done) {
            System.out.println("0: Rewrite the entire file");
            System.out.println("1: Append to the file\n");
            System.out.print("Choose file-writing mode: ");

            String userInput = scanner.nextLine();
            if (userInput.length() != 1) {
                System.out.println("Error: unacceptable user response");
                continue;
            }

            try {
                short resShort = Short.parseShort(userInput);
                if (resShort < 0 || resShort > 1) {
                    System.out.println("Error: out of bounds");
                    continue;
                }

                result = resShort == 1;
                done = true;
            } catch (NumberFormatException exception) {
                System.out.println(exception.getMessage());
            }
        }
        return result;
    }

    public boolean toSpecifyFormattingFile() {
        boolean res = false;
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        while (!done) {
            System.out.print("Specify formatting file path? (yes/no, no leaves default): ");

            String userInput = scanner.nextLine().strip().toLowerCase();

            switch (userInput) {
                case "no":
                    // res already false
                    done = true;
                    break;
                case "yes":
                    res = true;
                    done = true;
                    break;
                default:
                    System.out.println("Unacceptable response.\n");
                    break;
            }
        }

        return res;
    }

    public String askUserForFormattingFile(int id) {
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        String path = "";

        System.out.println("API " + id + ": ");
        while (!done) {
            System.out.print("Enter the name of the file in resources folder containing formatting: ");

            String userInput = scanner.nextLine().strip();
            path = userInput;

            if (Files.exists(Path.of(BASE_FILE_PATH + path))) {
                done = true;
            } else {
                System.out.println("File doesn't exist.\n");
            }
        }

        return path;
    }

    public boolean askUserWhetherInteractive() {
        boolean res = false;
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        while (!done) {
            System.out.println("1. Automatic");
            System.out.println("2. Interactive");
            System.out.print("Automatic or interactive: ");

            String userInput = scanner.nextLine();

            switch (userInput) {
                case "1":
                    // res already false
                    done = true;
                    break;
                case "2":
                    res = true;
                    done = true;
                    break;
                default:
                    System.out.println("Unacceptable response.\n");
                    break;
            }
        }

        return res;
    }

    public String askUserForAdditionalPath(ApiRecord apiRecord) {
        String[] paths = apiRecord.additionalPaths();

        int index = 0;

        System.out.println("Pick a path: ");
        for (int i = 0; i < paths.length; i++) {
            System.out.println((i + 1) + ": " + paths[i]);
        }

        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        while (!done) {
            System.out.print("\nChoose: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("random")) {
                index = new Random().nextInt(paths.length);
                break;
            }

            try {
                index = Integer.parseInt(userInput);
                index--; // since the user input starts with 1
                if (index >= 0 && index <= paths.length) {
                    done = true;
                } else {
                    System.out.println("Error: out of bounds!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: not a number!");
            }
        }

        return paths[index];
    }

    public Map<Integer, String> getIdsToFormattingFileFromUser(int[] apiIDs) {
        final String defaultFormattingFile = "outputFormat_%d.txt";

        Map<Integer, String> idToFormattingFile = new HashMap<>();

        if (this.toSpecifyFormattingFile()) {
            for (int id : apiIDs) {
                String formatFileName = this.askUserForFormattingFile(id);
                idToFormattingFile.put(id, formatFileName);
            }
        } else {
            for (int id : apiIDs) {
                idToFormattingFile.put(id, String.format(defaultFormattingFile, id));
            }
        }

        return idToFormattingFile;
    }
}
