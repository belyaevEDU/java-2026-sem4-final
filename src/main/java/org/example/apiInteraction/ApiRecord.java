package org.example.apiInteraction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public final class ApiRecord {
    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("key_needed")
    private boolean keyNeeded;
    @JsonProperty("API_KEY")
    private String apiKey;
    @JsonProperty("base_request_url")
    private String baseRequestURL;
    @JsonProperty("additional_path_needed")
    private boolean additionalPathNeeded;
    @JsonProperty("additional_paths")
    private String[] additionalPaths;
    @JsonProperty("headers")
    private Map<String, String> headers;

    public ApiRecord() {} // for jackson's objectMapper to work (same thing for the setters)

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean keyNeeded() {
        return keyNeeded;
    }

    protected String apiKey() {
        return apiKey;
    }

    public String baseRequestURL() {
        return baseRequestURL;
    }

    public boolean additionalPathNeeded() {
        return additionalPathNeeded;
    }

    public String[] additionalPaths() {
        return additionalPaths;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKeyNeeded(boolean keyNeeded) {
        this.keyNeeded = keyNeeded;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setBaseRequestURL(String baseRequestURL) {
        this.baseRequestURL = baseRequestURL;
    }

    public void setAdditionalPathNeeded(boolean additionalPathNeeded) {
        this.additionalPathNeeded = additionalPathNeeded;
    }

    public void setAdditionalPaths(String[] additionalPaths) {
        this.additionalPaths = additionalPaths;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    // 3 functions below gen'd by IDEA
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ApiRecord) obj;
        return this.id == that.id &&
                Objects.equals(this.name, that.name) &&
                this.keyNeeded == that.keyNeeded &&
                Objects.equals(this.apiKey, that.apiKey) &&
                Objects.equals(this.baseRequestURL, that.baseRequestURL) &&
                this.additionalPathNeeded == that.additionalPathNeeded &&
                Objects.equals(this.additionalPaths, that.additionalPaths) &&
                Objects.equals(this.headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, keyNeeded, apiKey, baseRequestURL, additionalPathNeeded, Arrays.hashCode(additionalPaths), headers);
    }

    @Override
    public String toString() {
        return "ApiRecord[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "keyNeeded=" + keyNeeded + ", " +
                "apiKey=" + apiKey + ", " +
                "baseRequestURL=" + baseRequestURL + ", " +
                "additionalPathNeeded=" + additionalPathNeeded + ", " +
                "additionalPaths=" + Arrays.toString(additionalPaths) + "]";
    }
}
