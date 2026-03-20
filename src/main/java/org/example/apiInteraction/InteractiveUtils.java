package org.example.apiInteraction;

import java.util.Scanner;

class InteractiveUtils {
    private final ApiRecord[] apis;

    InteractiveUtils(ApiRecord[] apis) {
        this.apis = apis;
    }

    int[] askUserForApis(String askMessage) {
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

    boolean askUserWhetherToAppend() {
        boolean done = false;
        Scanner scanner = new Scanner(System.in);

        boolean result = false;

        while (!done) {
            System.out.println("0: Rewrite the entire file");
            System.out.println("1: Append to the file\n");
            System.out.print("Choose file-writing mode:");

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
}
