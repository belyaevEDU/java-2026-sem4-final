package org.example.apiInteraction.resultFormatting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import static org.junit.jupiter.api.Assertions.*;

class JSONResultFormatterTest {

    private JSONResultFormatter formatter;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        formatter = new JSONResultFormatter("test-api");
        mapper = new ObjectMapper();
    }

    // format() on empty existing content

    @Test
    void format_emptyExisting_producesOneRecord() throws Exception {
        String payload = "{\"temp\": 22.5, \"city\": \"Berlin\"}";
        String result = formatter.format(payload, "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals(1, arr.size());
    }

    @Test
    void format_emptyExisting_recordHasId1() throws Exception {
        String payload = "{\"value\": 1}";
        String result = formatter.format(payload, "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals(1, arr.get(0).get("id").asInt());
    }

    @Test
    void format_emptyExisting_recordHasCorrectSource() throws Exception {
        formatter.setSourceName("weather");
        String result = formatter.format("{\"x\":1}", "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals("weather", arr.get(0).get("source").asText());
    }

    @Test
    void format_emptyExisting_recordHasTimestamp() throws Exception {
        String result = formatter.format("{\"x\":1}", "");
        ArrayNode arr = (ArrayNode) mapper.readTree(result);

        String ts = arr.get(0).get("timestamp").asText();
        // ISO 8601 contains 'T'
        assertTrue(ts.contains("T"), "Timestamp should be ISO 8601, got: " + ts);
    }

    @Test
    void format_emptyExisting_dataFieldMatchesPayload() throws Exception {
        String payload = "{\"temperature\": -5}";
        String result = formatter.format(payload, "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        JsonNode data = arr.get(0).get("data");
        assertEquals(-5, data.get("temperature").asInt());
    }

    // format() appending to existing records

    @Test
    void format_appendToExisting_incrementsId() throws Exception {
        String existing = "[{\"id\":1,\"source\":\"s\",\"timestamp\":\"t\",\"data\":{}}]";
        String result = formatter.format("{\"k\":\"v\"}", existing);

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals(2, arr.size());
        assertEquals(2, arr.get(1).get("id").asInt());
    }

    @Test
    void format_appendMultiple_idsAreSequential() throws Exception {
        String payload = "{\"v\":1}";
        String step1 = formatter.format(payload, "");
        String step2 = formatter.format(payload, step1);
        String step3 = formatter.format(payload, step2);

        ArrayNode arr = (ArrayNode) mapper.readTree(step3);
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0).get("id").asInt());
        assertEquals(2, arr.get(1).get("id").asInt());
        assertEquals(3, arr.get(2).get("id").asInt());
    }

    @Test
    void format_existingWithGapInIds_usesMaxPlusOne() throws Exception {
        String existing = "[{\"id\":5,\"source\":\"s\",\"timestamp\":\"t\",\"data\":{}}]";
        String result = formatter.format("{\"x\":0}", existing);

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals(6, arr.get(1).get("id").asInt());
    }

    // setSourceName

    @Test
    void setSourceName_changesSourceInOutput() throws Exception {
        formatter.setSourceName("movies");
        String result = formatter.format("{\"title\":\"Inception\"}", "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals("movies", arr.get(0).get("source").asText());
    }

    // nestedJson

    @Test
    void format_nestedPayload_dataPreservesStructure() throws Exception {
        String payload = "{\"location\":{\"city\":\"Moscow\",\"coords\":{\"lat\":55.75,\"lon\":37.62}}}";
        String result = formatter.format(payload, "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        JsonNode location = arr.get(0).get("data").get("location");
        assertEquals("Moscow", location.get("city").asText());
        assertEquals(55.75, location.get("coords").get("lat").asDouble(), 0.001);
    }

    @Test
    void format_arrayPayload_dataPreservesArray() throws Exception {
        String payload = "{\"items\":[{\"id\":1,\"name\":\"Milk\"},{\"id\":2,\"name\":\"Bread\"}]}";
        String result = formatter.format(payload, "");

        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        JsonNode items = arr.get(0).get("data").get("items");
        assertTrue(items.isArray());
        assertEquals(2, items.size());
    }

    // null / blank existing

    @Test
    void format_nullExisting_treatedAsEmpty() throws Exception {
        String result = formatter.format("{\"x\":1}", null);
        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals(1, arr.size());
        assertEquals(1, arr.get(0).get("id").asInt());
    }

    @Test
    void format_blankExisting_treatedAsEmpty() throws Exception {
        String result = formatter.format("{\"x\":1}", "   ");
        ArrayNode arr = (ArrayNode) mapper.readTree(result);
        assertEquals(1, arr.size());
    }

    // output is valid JSON

    @Test
    void format_outputIsValidJson() {
        String result = formatter.format("{\"a\":\"b\"}", "");
        assertDoesNotThrow(() -> mapper.readTree(result));
    }

    @Test
    void format_outputIsJsonArray() throws Exception {
        String result = formatter.format("{\"a\":\"b\"}", "");
        assertTrue(mapper.readTree(result).isArray());
    }
}
