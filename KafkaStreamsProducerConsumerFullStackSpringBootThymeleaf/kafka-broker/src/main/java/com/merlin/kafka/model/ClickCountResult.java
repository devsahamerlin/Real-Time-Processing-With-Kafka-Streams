package com.merlin.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;


public class ClickCountResult {
    @JsonProperty("key")
    private final String key; // Could be userId or "global"

    @JsonProperty("count")
    private final Long count;

    @JsonProperty("lastUpdated")
    private final String lastUpdated;

    public ClickCountResult() {
        this.key = null;
        this.count = null;
        this.lastUpdated = null;
    }

    public ClickCountResult(String key, Long count, String lastUpdated) {
        this.key = key;
        this.count = count;
        this.lastUpdated = lastUpdated;
    }

    public String getKey() { return key; }
    public Long getCount() { return count; }
    public String getLastUpdated() { return lastUpdated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickCountResult that = (ClickCountResult) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(count, that.count) &&
                Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, count, lastUpdated);
    }

    @Override
    public String toString() {
        return String.format("ClickCountResult{key='%s', count=%d, lastUpdated='%s'}",
                key, count, lastUpdated);
    }
}
