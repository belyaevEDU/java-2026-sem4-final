package org.example.apiInteraction.resultFormatting;

import org.jetbrains.annotations.NotNull;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JSONResultFormatter implements CustomFormatter {
    private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
    private String sourceName;
    private final ObjectMapper mapper;

    public JSONResultFormatter(String sourceName) {
        this.sourceName = sourceName;
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }


    public String format(String jsonPayload, String existingContent) {
        List<ObjectNode> records = parseExisting(existingContent);

        int nextId = maxId(records) + 1;

        JsonNode data = mapper.readTree(jsonPayload);
        records.add(buildEnvelope(data, nextId));

        return mapper.writeValueAsString(records);
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    private List<ObjectNode> parseExisting(String content) {
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }
        ArrayNode array = (ArrayNode) mapper.readTree(content);
        return mapper.convertValue(array, new TypeReference<>() {
        });
    }

    private int maxId(@NotNull List<ObjectNode> records) {
        return records.stream()
                .mapToInt(r -> r.path("id").asInt(0))
                .max()
                .orElse(0);
    }

    private @NotNull ObjectNode buildEnvelope(JsonNode data, int id) {
        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("id", id);
        envelope.put("source", sourceName);
        envelope.put("timestamp", currentTimestamp());
        envelope.set("data", data);

        return envelope;
    }

    private @NotNull String currentTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(ISO8601);
    }
}
