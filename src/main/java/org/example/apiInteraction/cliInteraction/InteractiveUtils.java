package org.example.apiInteraction.cliInteraction;

import org.example.apiInteraction.RunArgs;
import org.example.apiInteraction.apiHandling.ApiRecord;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class InteractiveUtils {
    private final ApiRecord[] apis;

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
                array = Arrays.stream(apis).mapToInt(ApiRecord::id).toArray();
                done = true;
                continue;
            }

            try {
                array = parseApiIds(userInput);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: non-numeric input.\n");
            }
        }

        return array;
    }

    public int[] askUserForApis(String askMessage, int[] filter) {
        final String allApisUserResponse = "all";
        final String noApiUserResponse = "none";

        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        int[] array = new int[]{};

        while (!done) {
            System.out.println("APIs:");
            for (ApiRecord api : apis) {
                if (Arrays.stream(filter).allMatch(e -> e != api.id())) {
                    continue;
                }
                System.out.println(api);
            }

            System.out.print(askMessage);
            String userInput = scanner.nextLine();

            switch (userInput) {
                case "" -> {
                    System.out.println("Error: user input is empty.\n");
                    continue;
                }
                case allApisUserResponse -> {
                    array = Arrays.stream(apis).mapToInt(ApiRecord::id).toArray();
                    done = true;
                    continue;
                }
                case noApiUserResponse -> {
                    array = new int[]{};
                    done = true;
                    continue;
                }
            }

            try {
                array = parseApiIds(userInput);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: non-numeric input.\n");
            }
        }

        return array;
    }

    private int[] parseApiIds(String userInput) throws NumberFormatException {
        String[] parts = userInput.split(",", -1);
        int[] ids = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String token = parts[i].trim();
            if (token.isEmpty()) {
                throw new NumberFormatException("empty ID given");
            }
            ids[i] = Integer.parseInt(token);
        }
        return ids;
    }

    public WriteMode askUserWhetherToAppend() {
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        WriteMode result = null;

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

                if (resShort == 0) {
                    result = WriteMode.OVERWRITE;
                } else {
                    result = WriteMode.APPEND;
                }
                done = true;
            } catch (NumberFormatException exception) {
                System.out.println(exception.getMessage());
            }
        }
        return result;
    }

    public RunArgs.FileType askUserForFileType() {
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        RunArgs.FileType result = null;

        while (!done) {
            System.out.println("0: JSON");
            System.out.println("1: CSV\n");
            System.out.print("Choose file type: ");

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

                if (resShort == 0) {
                    result = RunArgs.FileType.JSON;
                } else {
                    result = RunArgs.FileType.CSV;
                }
                done = true;
            } catch (NumberFormatException exception) {
                System.out.println(exception.getMessage());
            }
        }
        return result;
    }

    public String askUserForAdditionalPath(ApiRecord apiRecord) {
        String[] paths = apiRecord.additionalPaths();

        int index = 0;

        System.out.println("Pick a path (" + apiRecord.name() + "):");
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
                if (index >= 0 && index < paths.length) {
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

    public int askUserForMaxConcurrent() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Maximum number of simultaneous requests (n, e.g. 3): ");
            String userInput = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(userInput);
                if (value < 1) {
                    System.out.println("Error: n must be at least 1.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a positive integer.");
            }
        }
    }

    public int askUserForInterval() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Polling interval in seconds (t, e.g. 10): ");
            String userInput = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(userInput);
                if (value < 1) {
                    System.out.println("Error: t must be at least 1 second.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a positive integer.");
            }
        }
    }

}
