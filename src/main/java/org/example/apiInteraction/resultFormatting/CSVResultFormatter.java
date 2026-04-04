package org.example.apiInteraction.resultFormatting;

import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CSVResultFormatter implements CustomFormatter {
    private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
    private String sourceName;
    private final ObjectMapper mapper;

    public CSVResultFormatter(String sourceName) {
        this.sourceName = sourceName;
        this.mapper = new ObjectMapper();
    }


    @Override
    public String format(String jsonPayload, String existingContent) {
        CsvData existing = parseExisting(existingContent);

        int nextId = maxId(existing.rows()) + 1;

        JsonNode data = mapper.readTree(jsonPayload);
        ObjectNode envelope = buildEnvelope(data, nextId);

        List<LinkedHashMap<String, String>> newRows = flattenNode(envelope, "");

        LinkedHashSet<String> mergedHeaders = new LinkedHashSet<>(existing.headers());
        newRows.forEach(row -> mergedHeaders.addAll(row.keySet()));

        List<Map<String, String>> allRows = new ArrayList<>(existing.rows());
        allRows.addAll(newRows);

        return serializeCsv(new ArrayList<>(mergedHeaders), allRows);
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    private @NotNull ObjectNode buildEnvelope(JsonNode data, int id) {
        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("id", id);
        envelope.put("source", sourceName);
        envelope.put("timestamp", currentTimestamp());
        envelope.set("data", data);
        return envelope;
    }

    private List<LinkedHashMap<String, String>> flattenNode(@NotNull JsonNode node, String prefix) {
        if (node.isObject()) {
            List<LinkedHashMap<String, String>> result = new ArrayList<>();
            result.add(new LinkedHashMap<>());

            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                String childPath = prefix.isEmpty()
                        ? entry.getKey()
                        : prefix + "." + entry.getKey();

                List<LinkedHashMap<String, String>> childRows = flattenNode(entry.getValue(), childPath);

                if (childRows.size() == 1) {
                    // non-expanding field: merge into every current row
                    for (LinkedHashMap<String, String> row : result) {
                        row.putAll(childRows.getFirst());
                    }
                } else {
                    // array expansion: cross-product existing rows * child rows
                    List<LinkedHashMap<String, String>> next =
                            new ArrayList<>(result.size() * childRows.size());
                    for (LinkedHashMap<String, String> existingRow : result) {
                        for (LinkedHashMap<String, String> childRow : childRows) {
                            LinkedHashMap<String, String> merged = new LinkedHashMap<>(existingRow);
                            merged.putAll(childRow);
                            next.add(merged);
                        }
                    }
                    result = next;
                }
            }
            return result;

        } else if (node.isArray()) {
            boolean hasComplexElements = false;
            for (JsonNode elem : node) {
                if (elem.isObject() || elem.isArray()) { hasComplexElements = true; break; }
            }

            if (hasComplexElements) {
                // object array: explode - each element contributes its own rows
                List<LinkedHashMap<String, String>> rows = new ArrayList<>();
                for (JsonNode element : node) {
                    rows.addAll(flattenNode(element, prefix));
                }
                return rows;
            } else {
                // primitive array: collapse into a single "|"-delimited cell
                StringJoiner joiner = new StringJoiner("|");
                for (JsonNode elem : node) {
                    joiner.add(elem.isNull() ? "" : elem.asString());
                }
                LinkedHashMap<String, String> row = new LinkedHashMap<>();
                row.put(prefix, joiner.toString());
                return List.of(row);
            }

        } else {
            // primitive or null
            LinkedHashMap<String, String> row = new LinkedHashMap<>();
            row.put(prefix, node.isNull() ? "" : node.asString());
            return List.of(row);
        }
    }

    private record CsvData(List<String> headers, List<Map<String, String>> rows) {
        static CsvData empty() {
            return new CsvData(new ArrayList<>(), new ArrayList<>());
        }
    }

    private @NotNull CsvData parseExisting(String content) {
        if (content == null || content.isBlank()) return CsvData.empty();

        String[] lines = content.split("\r?\n", -1);
        if (lines.length == 0) return CsvData.empty();

        List<String> headers = parseCsvLine(lines[0]);
        List<Map<String, String>> rows = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) continue;
            List<String> values = parseCsvLine(lines[i]);
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                row.put(headers.get(j), j < values.size() ? values.get(j) : "");
            }
            rows.add(row);
        }
        return new CsvData(headers, rows);
    }

    private int maxId(@NotNull List<Map<String, String>> rows) {
        return rows.stream()
                .mapToInt(r -> {
                    try { return Integer.parseInt(r.getOrDefault("id", "0")); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max()
                .orElse(0);
    }

    private @NotNull String serializeCsv(@NotNull List<String> headers, @NotNull List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();

        StringJoiner headerLine = new StringJoiner(",");
        headers.forEach(h -> headerLine.add(escapeCsv(h)));
        sb.append(headerLine).append("\n");

        for (Map<String, String> row : rows) {
            StringJoiner dataLine = new StringJoiner(",");
            for (String header : headers) {
                dataLine.add(escapeCsv(row.getOrDefault(header, "")));
            }
            sb.append(dataLine).append("\n");
        }

        return sb.toString();
    }

    private @NotNull List<String> parseCsvLine(@NotNull String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private @NotNull String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private @NotNull String currentTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(ISO8601);
    }

}
