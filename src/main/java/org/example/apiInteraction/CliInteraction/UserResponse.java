package org.example.apiInteraction.CliInteraction;

import java.util.Map;

public record UserResponse(
        Map<Integer, String> idToFormatFile,
        boolean toAppend,
        int[] outputApiIDs
) {}
