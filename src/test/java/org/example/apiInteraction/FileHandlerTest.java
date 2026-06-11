package org.example.apiInteraction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @TempDir
    Path tempDir;

    // write()

    @Test
    void write_createsFileIfNotExist(@TempDir Path dir) throws IOException {
        File target = dir.resolve("output.json").toFile();
        assertFalse(target.exists());

        FileHandler fh = new FileHandler(target.getAbsolutePath());
        fh.write("{\"x\":1}");

        assertTrue(target.exists());
    }

    @Test
    void write_contentMatchesWhatWasWritten(@TempDir Path dir) throws IOException {
        String content = "[{\"id\":1,\"source\":\"test\"}]";
        File target = dir.resolve("data.json").toFile();

        FileHandler fh = new FileHandler(target.getAbsolutePath());
        fh.write(content);

        String actual = Files.readString(target.toPath());
        assertEquals(content, actual);
    }

    @Test
    void write_overwritesPreviousContent(@TempDir Path dir) throws IOException {
        File target = dir.resolve("data.json").toFile();
        FileHandler fh = new FileHandler(target.getAbsolutePath());

        fh.write("old content");
        fh.write("new content");

        assertEquals("new content", Files.readString(target.toPath()));
    }

    @Test
    void write_emptyString_createsEmptyFile(@TempDir Path dir) throws IOException {
        File target = dir.resolve("empty.txt").toFile();
        FileHandler fh = new FileHandler(target.getAbsolutePath());
        fh.write("");

        assertEquals("", Files.readString(target.toPath()));
    }

    @Test
    void write_createsParentDirectories(@TempDir Path dir) throws IOException {
        File target = dir.resolve("nested/deeply/output.json").toFile();
        FileHandler fh = new FileHandler(target.getAbsolutePath());
        fh.write("hello");

        assertTrue(target.exists());
        assertEquals("hello", Files.readString(target.toPath()));
    }

    // read()

    @Test
    void read_nonExistentFile_returnsEmpty(@TempDir Path dir) throws IOException {
        FileHandler fh = new FileHandler(dir.resolve("missing.json").toString());
        assertEquals("", fh.read());
    }

    @Test
    void read_emptyFile_returnsEmpty(@TempDir Path dir) throws IOException {
        File target = dir.resolve("empty.json").toFile();
        target.createNewFile();

        FileHandler fh = new FileHandler(target.getAbsolutePath());
        assertEquals("", fh.read());
    }

    @Test
    void read_afterWrite_returnsWrittenContent(@TempDir Path dir) throws IOException {
        String content = "{\"key\":\"value\"}";
        File target = dir.resolve("file.json").toFile();
        FileHandler fh = new FileHandler(target.getAbsolutePath());

        fh.write(content);
        assertEquals(content, fh.read());
    }

    @Test
    void read_multilineContent_preservedExactly(@TempDir Path dir) throws IOException {
        String content = "line1\nline2\nline3";
        File target = dir.resolve("multi.txt").toFile();
        FileHandler fh = new FileHandler(target.getAbsolutePath());

        fh.write(content);
        assertEquals(content, fh.read());
    }

    @Test
    void read_unicodeContent_preservedExactly(@TempDir Path dir) throws IOException {
        String content = "Москва: {\"temp\": -5.2}";
        File target = dir.resolve("unicode.json").toFile();
        FileHandler fh = new FileHandler(target.getAbsolutePath());

        fh.write(content);
        assertEquals(content, fh.read());
    }
}
