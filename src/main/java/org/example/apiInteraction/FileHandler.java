package org.example.apiInteraction;

import org.example.apiInteraction.cliInteraction.WriteMode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileHandler {
    private final File file;

    public FileHandler(String filePath) {
        this.file = new File(filePath);
    }


    public String read() throws IOException {
        if (!file.exists() || file.length() == 0) return "";
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public void write(String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}