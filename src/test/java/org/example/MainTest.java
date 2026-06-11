package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    // ---- enumToString ----

    @Test
    void enumToString_interactive_returnsLowercase() {
        String result = Main.enumToString(org.example.apiInteraction.RunArgs.UserInteractionType.INTERACTIVE);
        assertEquals("interactive", result);
    }

    @Test
    void enumToString_automatic_returnsLowercase() {
        String result = Main.enumToString(org.example.apiInteraction.RunArgs.UserInteractionType.AUTOMATIC);
        assertEquals("automatic", result);
    }

    @Test
    void enumToString_csv_returnsLowercase() {
        String result = Main.enumToString(org.example.apiInteraction.RunArgs.FileType.CSV);
        assertEquals("csv", result);
    }

    @Test
    void enumToString_json_returnsLowercase() {
        String result = Main.enumToString(org.example.apiInteraction.RunArgs.FileType.JSON);
        assertEquals("json", result);
    }

    // ---- main() — argument validation (no actual interaction started) ----

    @Test
    void main_noArgs_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{}));
    }

    @Test
    void main_unknownMode_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"bogus"}));
    }

    @Test
    void main_automaticWrongArgCount_throwsIllegalArgument() {
        // automatic needs exactly 4 args: automatic <format> <n> <t>
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"automatic", "json", "2"}));
    }

    @Test
    void main_automaticBadFormat_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"automatic", "xml", "2", "5"}));
    }

    @Test
    void main_automaticNegativeN_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"automatic", "json", "-1", "5"}));
    }

    @Test
    void main_automaticNegativeT_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"automatic", "csv", "2", "-3"}));
    }

    @Test
    void main_automaticNonIntegerN_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"automatic", "json", "abc", "5"}));
    }

    @Test
    void main_automaticNonIntegerT_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"automatic", "json", "2", "xyz"}));
    }

    @Test
    void main_interactiveWithExtraArgs_throwsIllegalArgument() {
        // interactive expects exactly 1 arg
        assertThrows(IllegalArgumentException.class,
                () -> Main.main(new String[]{"interactive", "extra"}));
    }
}
