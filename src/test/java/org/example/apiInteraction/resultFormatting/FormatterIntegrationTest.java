package org.example.apiInteraction.resultFormatting;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatterIntegrationTest {

    private static final String SAMOKAT_PAYLOAD = """
            {
              "id": 12,
              "timestamp": "2024-01-06T16:45:22+00:00",
              "customer": {
                "id": 1,
                "name": "Petr",
                "surname": "Ivanov",
                "phone_number": "+70123456789"
              },
              "delivery_address": {
                "city": "Saint-Petersburg",
                "street": "Nevskiy pr.",
                "home": 3
              },
              "items": [
                {"id": 1, "name": "Milk Parmalat", "price": 124},
                {"id": 2, "name": "Bread Harris",  "price": 85}
              ]
            }
            """;

    private static final String WEATHER_PAYLOAD = """
            {
              "id": 14,
              "timestamp": "2024-01-15T14:30:00+03:00",
              "location": {
                "city": "Москва",
                "country": "Россия",
                "coordinates": {"latitude": 55.7558, "longitude": 37.6173},
                "timezone": "Europe/Moscow"
              },
              "current_weather": {
                "temperature": {"celsius": -5.2, "fahrenheit": 22.6}
              }
            }
            """;

    // JSON formatter

    @Test
    void json_samokatRecord_hasCorrectSource() throws Exception {
        JSONResultFormatter fmt = new JSONResultFormatter("samokat");
        String result = fmt.format(SAMOKAT_PAYLOAD, "");

        ObjectMapper m = new ObjectMapper();
        ArrayNode arr = (ArrayNode) m.readTree(result);
        assertEquals("samokat", arr.get(0).get("source").asText());
    }

    @Test
    void json_samokatRecord_dataPreservesCustomerName() throws Exception {
        JSONResultFormatter fmt = new JSONResultFormatter("samokat");
        String result = fmt.format(SAMOKAT_PAYLOAD, "");

        ObjectMapper m = new ObjectMapper();
        JsonNode data = m.readTree(result).get(0).get("data");
        assertEquals("Petr", data.get("customer").get("name").asText());
    }

    @Test
    void json_samokatRecord_dataPreservesItemsArray() throws Exception {
        JSONResultFormatter fmt = new JSONResultFormatter("samokat");
        String result = fmt.format(SAMOKAT_PAYLOAD, "");

        ObjectMapper m = new ObjectMapper();
        JsonNode items = m.readTree(result).get(0).get("data").get("items");
        assertTrue(items.isArray());
        assertEquals(2, items.size());
    }

    @Test
    void json_weatherAfterSamokat_hasTwoRecords() throws Exception {
        JSONResultFormatter fmt = new JSONResultFormatter("samokat");
        String after1 = fmt.format(SAMOKAT_PAYLOAD, "");

        fmt.setSourceName("yandex_weather");
        String after2 = fmt.format(WEATHER_PAYLOAD, after1);

        ObjectMapper m = new ObjectMapper();
        assertEquals(2, m.readTree(after2).size());
    }

    @Test
    void json_weatherRecord_preservesCoordinates() throws Exception {
        JSONResultFormatter fmt = new JSONResultFormatter("yandex_weather");
        String result = fmt.format(WEATHER_PAYLOAD, "");

        ObjectMapper m = new ObjectMapper();
        JsonNode coords = m.readTree(result).get(0).get("data")
                .get("location").get("coordinates");
        assertEquals(55.7558, coords.get("latitude").asDouble(), 0.0001);
        assertEquals(37.6173, coords.get("longitude").asDouble(), 0.0001);
    }

    @Test
    void json_weatherRecord_preservesTemperature() throws Exception {
        JSONResultFormatter fmt = new JSONResultFormatter("yandex_weather");
        String result = fmt.format(WEATHER_PAYLOAD, "");

        ObjectMapper m = new ObjectMapper();
        JsonNode temp = m.readTree(result).get(0).get("data")
                .get("current_weather").get("temperature");
        assertEquals(-5.2, temp.get("celsius").asDouble(), 0.01);
    }

    // CSV formatter

    @Test
    void csv_samokatWithItems_expandsToTwoRows() {
        CSVResultFormatter fmt = new CSVResultFormatter("samokat");
        String result = fmt.format(SAMOKAT_PAYLOAD, "");

        // header + 2 rows (one per item)
        List<String> lines = Arrays.stream(result.split("\r?\n"))
                .filter(l -> !l.isBlank()).toList();
        assertEquals(3, lines.size());
    }

    @Test
    void csv_samokatHeader_containsCustomerName() {
        CSVResultFormatter fmt = new CSVResultFormatter("samokat");
        String result = fmt.format(SAMOKAT_PAYLOAD, "");
        String header = result.split("\r?\n")[0];
        assertTrue(header.contains("data.customer.name"), "header: " + header);
    }

    @Test
    void csv_twoSources_mergedHeaders() {
        CSVResultFormatter fmt = new CSVResultFormatter("samokat");
        String after1 = fmt.format(SAMOKAT_PAYLOAD, "");

        fmt.setSourceName("yandex_weather");
        String after2 = fmt.format(WEATHER_PAYLOAD, after1);

        String header = after2.split("\r?\n")[0];
        assertTrue(header.contains("data.customer.name"),    "header: " + header);
        assertTrue(header.contains("data.location.city"),    "header: " + header);
    }

    @Test
    void csv_weatherRow_containsMoscow() {
        CSVResultFormatter fmt = new CSVResultFormatter("yandex_weather");
        String result = fmt.format(WEATHER_PAYLOAD, "");
        // Москва appears somewhere in the data rows
        assertTrue(result.contains("Москва"), "result: " + result);
    }

    @Test
    void csv_appendPreservesExistingRows() {
        CSVResultFormatter fmt = new CSVResultFormatter("samokat");
        String after1 = fmt.format(SAMOKAT_PAYLOAD, "");

        fmt.setSourceName("yandex_weather");
        String after2 = fmt.format(WEATHER_PAYLOAD, after1);

        List<String> lines = Arrays.stream(after2.split("\r?\n"))
                .filter(l -> !l.isBlank()).toList();

        // samokat expands to 2 rows + weather = 1 row -> header + 3 data rows
        assertEquals(4, lines.size());
    }
}
