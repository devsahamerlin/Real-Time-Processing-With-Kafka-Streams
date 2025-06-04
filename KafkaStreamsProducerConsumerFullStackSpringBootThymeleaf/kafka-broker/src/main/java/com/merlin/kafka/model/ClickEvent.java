package com.merlin.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

public class ClickEvent {
    @JsonProperty("userId")
    private final String userId;

    @JsonProperty("action")
    private final String action;

    @JsonProperty("timestamp")
    private final LocalDateTime timestamp;

    public ClickEvent() {
        this.userId = null;
        this.action = null;
        this.timestamp = null;
    }

    public ClickEvent(String userId, String action, LocalDateTime timestamp) {
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public static ClickEvent createClick(String userId) {
        return new ClickEvent(userId, "click", LocalDateTime.now());
    }

    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickEvent that = (ClickEvent) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(action, that.action) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, action, timestamp);
    }

    @Override
    public String toString() {
        return String.format("ClickEvent{userId='%s', action='%s', timestamp=%s}",
                userId, action, timestamp);
    }
}

