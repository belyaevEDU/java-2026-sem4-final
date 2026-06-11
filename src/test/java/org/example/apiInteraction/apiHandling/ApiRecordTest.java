package org.example.apiInteraction.apiHandling;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiRecordTest {

    private ApiRecord buildRecord(int id, String name, boolean keyNeeded,
                                  String key, String baseUrl,
                                  boolean additionalNeeded, String[] paths,
                                  Map<String, String> headers) {
        ApiRecord r = new ApiRecord();
        r.setId(id);
        r.setName(name);
        r.setKeyNeeded(keyNeeded);
        r.setApiKey(key);
        r.setBaseRequestURL(baseUrl);
        r.setAdditionalPathNeeded(additionalNeeded);
        r.setAdditionalPaths(paths);
        r.setHeaders(headers);
        return r;
    }

    @Test
    void gettersReturnSetValues() {
        Map<String, String> headers = Map.of("accept", "application/json");
        ApiRecord r = buildRecord(1, "TestAPI", true, "secret-key",
                "https://example.com/api/", true,
                new String[]{"foo", "bar"}, headers);

        assertEquals(1, r.id());
        assertEquals("TestAPI", r.name());
        assertTrue(r.keyNeeded());
        assertEquals("https://example.com/api/", r.baseRequestURL());
        assertTrue(r.additionalPathNeeded());
        assertArrayEquals(new String[]{"foo", "bar"}, r.additionalPaths());
        assertEquals(headers, r.headers());
    }

    @Test
    void equalsAndHashCode_sameData_equal() {
        ApiRecord a = buildRecord(1, "API", false, null, "https://x.com/", false, null, null);
        ApiRecord b = buildRecord(1, "API", false, null, "https://x.com/", false, null, null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsAndHashCode_differentId_notEqual() {
        ApiRecord a = buildRecord(1, "API", false, null, "https://x.com/", false, null, null);
        ApiRecord b = buildRecord(2, "API", false, null, "https://x.com/", false, null, null);

        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        ApiRecord r = buildRecord(1, "API", false, null, "https://x.com/", false, null, null);
        assertNotEquals(null, r);
    }

    @Test
    void equals_sameReference_returnsTrue() {
        ApiRecord r = buildRecord(1, "API", false, null, "https://x.com/", false, null, null);
        assertEquals(r, r);
    }

    @Test
    void equals_differentClass_returnsFalse() {
        ApiRecord r = buildRecord(1, "API", false, null, "https://x.com/", false, null, null);
        assertNotEquals("not an ApiRecord", r);
    }

    @Test
    void toString_containsIdAndName() {
        ApiRecord r = buildRecord(42, "WeatherAPI", false, null, "https://w.com/", false, null, null);
        String str = r.toString();
        assertTrue(str.contains("42"));
        assertTrue(str.contains("WeatherAPI"));
    }

    @Test
    void nullFields_doNotThrow() {
        ApiRecord r = new ApiRecord();
        assertDoesNotThrow(r::id);
        assertDoesNotThrow(r::name);
        assertNull(r.name());
        assertNull(r.additionalPaths());
        assertNull(r.headers());
    }

    @Test
    void additionalPathsArray_equalsByContent() {
        ApiRecord a = buildRecord(1, "A", false, null, "u", true, new String[]{"x"}, null);
        ApiRecord b = buildRecord(1, "A", false, null, "u", true, new String[]{"x"}, null);
        assertEquals(a, b);
    }

    @Test
    void additionalPathsArray_differentContent_notEqual() {
        ApiRecord a = buildRecord(1, "A", false, null, "u", true, new String[]{"x"}, null);
        ApiRecord b = buildRecord(1, "A", false, null, "u", true, new String[]{"y"}, null);
        assertNotEquals(a, b);
    }
}
