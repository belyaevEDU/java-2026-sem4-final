package org.example.apiInteraction;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputFormatter {
    private final Pattern PLACEHOLDER_PATTERN;
    private final String outputFormatFile;
    private String dataIn;

    public OutputFormatter(String outputFormatFile, String dataIn) {
        this.PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
        this.outputFormatFile = outputFormatFile;
        this.dataIn = dataIn;
    }

    public String format() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> rootData = mapper.readValue(dataIn, new TypeReference<>() {});

        String outputFormat;
        try {
            outputFormat = Files.readString(Path.of(outputFormatFile));
        } catch (IOException e) {
            System.out.println("Error:" + e.getMessage());
            return null;
        }

        return parseAndFormat(outputFormat, rootData);
    }

    private String parseAndFormat(String template, Map<?, ?> rootData) {
        var finalOutput = new StringBuilder();
        var arrayTemplateLines = new ArrayList<String>();

        boolean inArray = false;
        String currentArrayName = null;

        for (String line : template.lines().toList()) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("\\ ARRAY ")) {
                inArray = true;
                currentArrayName = trimmedLine.substring("\\ ARRAY ".length()).trim();
                arrayTemplateLines.clear();
            } else if (trimmedLine.equals("\\") && inArray) {
                inArray = false;

                Object arrayObj = rootData.get(currentArrayName);

                if (arrayObj instanceof List<?> list) {
                    String arrayTemplate = String.join(System.lineSeparator(), arrayTemplateLines);

                    for (Object element : list) {
                        if (element instanceof Map<?, ?> contextMap) {
                            finalOutput.append(resolvePlaceholders(arrayTemplate, contextMap))
                                    .append(System.lineSeparator());
                        }
                    }
                }
            } else {
                if (inArray) {
                    arrayTemplateLines.add(line);
                } else {
                    finalOutput.append(resolvePlaceholders(line, rootData)).append(System.lineSeparator());
                }
            }
        }

        return finalOutput.toString().trim();
    }

    private String resolvePlaceholders(String text, Map<?, ?> contextMap) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        var sb = new StringBuilder();

        while (matcher.find()) {
            String path = matcher.group(1);
            Object val = getNestedValue(contextMap, path);

            String replacement = (val != null) ? String.valueOf(val) : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static Object getNestedValue(Map<?, ?> map, String path) {
        String[] keys = path.split("\\.");
        Object current = map;

        for (String key : keys) {
            if (current instanceof Map<?, ?> currentMap) {
                current = currentMap.get(key);
            } else {
                return null;
            }
        }
        return current;
    }

}
