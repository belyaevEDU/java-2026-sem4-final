package org.example.apiInteraction.resultFormatting;

import java.io.IOException;
import java.util.List;

public interface CustomFormatter {
    String format(String payload, String existingContent) throws IOException;
    void setSourceName(String sourceName);
}
