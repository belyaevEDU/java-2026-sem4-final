package org.example.apiInteraction;

import org.example.apiInteraction.RunArgs.FileType;
import org.example.apiInteraction.RunArgs.UserInteractionType;
import org.example.apiInteraction.cliInteraction.UserResponse;
import org.example.apiInteraction.cliInteraction.WriteMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunArgsTest {

    @Test
    void fileType_csvExists() {
        assertNotNull(FileType.CSV);
    }

    @Test
    void fileType_jsonExists() {
        assertNotNull(FileType.JSON);
    }

    @Test
    void fileType_values_containsBothTypes() {
        FileType[] values = FileType.values();
        assertEquals(2, values.length);
    }

    @Test
    void userInteractionType_interactiveExists() {
        assertNotNull(UserInteractionType.INTERACTIVE);
    }

    @Test
    void userInteractionType_automaticExists() {
        assertNotNull(UserInteractionType.AUTOMATIC);
    }

    @Test
    void fileType_valueOf_csv() {
        assertEquals(FileType.CSV, FileType.valueOf("CSV"));
    }

    @Test
    void fileType_valueOf_json() {
        assertEquals(FileType.JSON, FileType.valueOf("JSON"));
    }

    @Test
    void userInteractionType_values_containsBothTypes() {
        assertEquals(2, UserInteractionType.values().length);
    }

    // UserResponse record

    @Test
    void userResponse_storesApiIds() {
        int[] ids = {1, 2};
        UserResponse r = new UserResponse(ids, WriteMode.OVERWRITE, new int[]{});
        assertArrayEquals(ids, r.apiIdsToCall());
    }

    @Test
    void userResponse_storesWriteMode() {
        UserResponse r = new UserResponse(new int[]{}, WriteMode.APPEND, new int[]{});
        assertEquals(WriteMode.APPEND, r.writeMode());
    }

    @Test
    void userResponse_storesOutputIds() {
        int[] outputIds = {0, 2};
        UserResponse r = new UserResponse(new int[]{}, WriteMode.OVERWRITE, outputIds);
        assertArrayEquals(outputIds, r.outputApiIDs());
    }

    // WriteMode enum

    @Test
    void writeMode_appendExists() {
        assertNotNull(WriteMode.APPEND);
    }

    @Test
    void writeMode_overwriteExists() {
        assertNotNull(WriteMode.OVERWRITE);
    }

    @Test
    void writeMode_values_containsBothModes() {
        assertEquals(2, WriteMode.values().length);
    }
}
