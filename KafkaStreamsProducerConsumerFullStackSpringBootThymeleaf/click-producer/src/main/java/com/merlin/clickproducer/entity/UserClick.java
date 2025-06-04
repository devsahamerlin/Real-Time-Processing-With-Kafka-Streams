package com.merlin.clickproducer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_clicks")
public class UserClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "click_timestamp", nullable = false)
    private LocalDateTime clickTimestamp;

    @Column(name = "session_id")
    private String sessionId;

    public UserClick() {}

    public UserClick(String userId, LocalDateTime clickTimestamp, String sessionId) {
        this.userId = userId;
        this.clickTimestamp = clickTimestamp;
        this.sessionId = sessionId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getClickTimestamp() { return clickTimestamp; }
    public void setClickTimestamp(LocalDateTime clickTimestamp) { this.clickTimestamp = clickTimestamp; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserClick userClick = (UserClick) o;
        return Objects.equals(id, userClick.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("UserClick{id=%d, userId='%s', clickTimestamp=%s, sessionId='%s'}",
                id, userId, clickTimestamp, sessionId);
    }
}