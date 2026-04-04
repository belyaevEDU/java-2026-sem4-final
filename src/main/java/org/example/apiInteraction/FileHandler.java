package org.example.apiInteraction;

import java.io.*;
import java.nio.file.Files;

public final class FileHandler {
    private final File file;

    public FileHandler(String filePath) {
        this.file = new File(filePath);
    }


    public String read() throws IOException {
        if (!file.exists() || file.length() == 0) return "";
        return Files.readString(file.toPath());
    }

    public void write(String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file))) {
            writer.write(content);
        }
    }
}