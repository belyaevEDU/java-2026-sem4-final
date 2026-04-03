package org.example.apiInteraction.cliInteraction;

public record UserResponse(
        int[] apiIdsToCall,
        WriteMode writeMode,
        int[] outputApiIDs
) {}
