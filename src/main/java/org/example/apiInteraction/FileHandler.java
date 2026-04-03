package org.example.apiInteraction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {
    public static void writeToFile(String data, boolean toAppend, String filePath) {
        try {
            File file = new File(filePath);
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            if (toAppend) {
                fileWriter.append("\n");
                fileWriter.append(data);
            } else {
                fileWriter.write(data);
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
