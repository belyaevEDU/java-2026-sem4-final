package org.example.apiInteraction.resultFormatting;

public interface CustomFormatter {
    String format(String payload, String existingContent);
    void setSourceName(String sourceName);
}
