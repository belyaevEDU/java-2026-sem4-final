package org.example.apiInteraction.resultFormatting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVResultFormatterTest {

    private CSVResultFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new CSVResultFormatter("test-api");
    }

    // helpers

    private List<String> lines(String csv) {
        return Arrays.stream(csv.split("\r?\n"))
                .filter(l -> !l.isBlank())
                .toList();
    }

    private String[] headerFields(String csv) {
        return lines(csv).get(0).split(",", -1);
    }

    // format() on empty existing

    @Test
    void format_emptyExisting_hasHeaderRow() {
        String result = formatter.format("{\"city\":\"Berlin\"}", "");
        assertTrue(lines(result).size() >= 1);
    }

    @Test
    void format_emptyExisting_headerContainsIdSourceTimestamp() {
        String result = formatter.format("{\"city\":\"Berlin\"}", "");
        String header = lines(result).get(0);
        assertTrue(header.contains("id"),        "header: " + header);
        assertTrue(header.contains("source"),    "header: " + header);
        assertTrue(header.contains("timestamp"), "header: " + header);
    }

    @Test
    void format_emptyExisting_dataRowHasId1() {
        String result = formatter.format("{\"temp\":22}", "");
        assertTrue(lines(result).get(1).startsWith("1,"), "data row: " + lines(result).get(1));
    }

    @Test
    void format_emptyExisting_sourceNameInDataRow() {
        formatter.setSourceName("weather");
        String result = formatter.format("{\"temp\":22}", "");
        assertTrue(lines(result).get(1).contains("weather"));
    }

    @Test
    void format_emptyExisting_flatFieldAppearsInHeader() {
        String result = formatter.format("{\"temperature\":5}", "");
        assertTrue(lines(result).get(0).contains("data.temperature"),
                "header: " + lines(result).get(0));
    }

    @Test
    void format_emptyExisting_flatFieldValueInDataRow() {
        String result = formatter.format("{\"temperature\":5}", "");
        String[] headers = headerFields(result);
        String[] dataFields = lines(result).get(1).split(",", -1);

        int idx = -1;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals("data.temperature")) { idx = i; break; }
        }
        assertTrue(idx >= 0, "Column data.temperature not found");
        assertEquals("5", dataFields[idx]);
    }

    // appending

    @Test
    void format_appendToExisting_rowCountIncreases() {
        String first  = formatter.format("{\"v\":1}", "");
        String second = formatter.format("{\"v\":2}", first);
        assertEquals(3, lines(second).size()); // header + 2 data rows
    }

    @Test
    void format_appendToExisting_idIncrements() {
        String first  = formatter.format("{\"v\":1}", "");
        String second = formatter.format("{\"v\":2}", first);
        assertTrue(lines(second).get(2).startsWith("2,"),
                "second data row: " + lines(second).get(2));
    }

    @Test
    void format_appendMultiple_idSequential() {
        String s1 = formatter.format("{\"v\":1}", "");
        String s2 = formatter.format("{\"v\":2}", s1);
        String s3 = formatter.format("{\"v\":3}", s2);
        List<String> rows = lines(s3);
        assertTrue(rows.get(1).startsWith("1,"));
        assertTrue(rows.get(2).startsWith("2,"));
        assertTrue(rows.get(3).startsWith("3,"));
    }

    // nested objects

    @Test
    void format_nestedObject_dotNotationInHeader() {
        String payload = "{\"location\":{\"city\":\"Moscow\"}}";
        assertTrue(lines(formatter.format(payload, "")).get(0).contains("data.location.city"),
                "header: " + lines(formatter.format(payload, "")).get(0));
    }

    @Test
    void format_nestedObject_valueInRow() {
        String payload = "{\"location\":{\"city\":\"Moscow\"}}";
        String result  = formatter.format(payload, "");
        String[] headers = headerFields(result);
        String[] data    = lines(result).get(1).split(",", -1);

        int idx = -1;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals("data.location.city")) { idx = i; break; }
        }
        assertTrue(idx >= 0, "Column data.location.city not found");
        assertEquals("Moscow", data[idx]);
    }

    // array expansion

    @Test
    void format_arrayOfObjects_expandsToMultipleRows() {
        String payload = "{\"items\":[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"}]}";
        assertEquals(3, lines(formatter.format(payload, "")).size()); // header + 2 rows
    }

    @Test
    void format_primitiveArray_collapsedWithPipe() {
        String payload = "{\"tags\":[\"java\",\"oop\",\"concurrent\"]}";
        assertTrue(formatter.format(payload, "").contains("java|oop|concurrent"));
    }

    // merging headers from different sources

    @Test
    void format_differentSources_headersMerged() {
        formatter.setSourceName("source1");
        String first = formatter.format("{\"field_a\":1}", "");
        formatter.setSourceName("source2");
        String second = formatter.format("{\"field_b\":2}", first);

        String header = lines(second).get(0);
        assertTrue(header.contains("data.field_a"), "merged header: " + header);
        assertTrue(header.contains("data.field_b"), "merged header: " + header);
    }

    @Test
    void format_differentSources_missingFieldIsEmpty() {
        formatter.setSourceName("source1");
        String first = formatter.format("{\"field_a\":1}", "");
        formatter.setSourceName("source2");
        String second = formatter.format("{\"field_b\":2}", first);

        String[] headers = headerFields(second);
        int idxA = -1;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals("data.field_a")) { idxA = i; break; }
        }
        assertTrue(idxA >= 0);
        assertEquals("", lines(second).get(2).split(",", -1)[idxA]);
    }

    // CSV escaping

    @Test
    void format_valueWithComma_escapedWithQuotes() {
        String payload = "{\"address\":\"Baker Street, 221B\"}";
        assertTrue(formatter.format(payload, "").contains("\"Baker Street, 221B\""));
    }

    @Test
    void format_valueWithQuote_doubledQuote() {
        String payload = "{\"note\":\"say \\\"hello\\\"\"}";
        assertTrue(formatter.format(payload, "").contains("\"\""));
    }

    // round-trip: quoted fields survive a read-then-append cycle

    /**
     * Write a value containing a comma (gets quoted in CSV), then append to
     * that CSV. Forces parseCsvLine to handle the quote-open/close toggle
     * and preserves the original value.
     */
    @Test
    void roundTrip_quotedFieldInExisting_parsedAndPreserved() {
        String first  = formatter.format("{\"address\":\"Baker St, 221B\"}", "");
        String second = formatter.format("{\"address\":\"Other St\"}", first);

        assertEquals(3, lines(second).size()); // header + 2 data rows
        assertTrue(second.contains("Baker St, 221B"),
                "Original comma-containing value should survive round-trip");
    }

    /**
     * Write a value containing a double-quote (CSV encodes as ""), then
     * append. Forces parseCsvLine to handle the doubled-quote path.
     */
    @Test
    void roundTrip_doubledQuoteInExisting_parsedAndPreserved() {
        String first  = formatter.format("{\"note\":\"say \\\"hello\\\"\"}", "");
        String second = formatter.format("{\"note\":\"other\"}", first);

        assertEquals(3, lines(second).size());
        // Either form of the value (raw or re-escaped) must appear
        assertTrue(second.contains("say \"hello\"") || second.contains("say \"\"hello\"\""),
                "Quoted value should survive round-trip: " + second);
    }

    // maxId NumberFormatException path

    /**
     * Existing CSV has a non-integer in the id column. The catch block
     * silently returns 0, so the new record receives id=1.
     */
    @Test
    void format_existingCsvWithBadId_fallsBackToId1() {
        String badIdCsv = "id,source,timestamp,data.val\n" +
                "NOT_A_NUMBER,src,2026-01-01T00:00:00+00:00,42\n";
        String result = formatter.format("{\"val\":99}", badIdCsv);

        List<String> rows = lines(result);
        assertEquals(3, rows.size()); // header + original row + new row
        assertTrue(rows.get(2).startsWith("1,"),
                "New row should have id=1 when existing id is non-parseable: " + rows.get(2));
    }

    // setSourceName

    @Test
    void setSourceName_changesSourceInOutput() {
        formatter.setSourceName("exchange-rate");
        assertTrue(formatter.format("{\"rate\":1.05}", "").contains("exchange-rate"));
    }

    // null / blank existing

    @Test
    void format_nullExisting_treatedAsEmpty() {
        String result = formatter.format("{\"x\":1}", null);
        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(lines(result).get(1).startsWith("1,"));
    }

    @Test
    void format_blankExisting_treatedAsEmpty() {
        String result = formatter.format("{\"x\":1}", "   \n  ");
        assertNotNull(result);
        assertTrue(lines(result).get(1).startsWith("1,"));
    }
}
