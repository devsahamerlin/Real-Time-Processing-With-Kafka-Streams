package com.merlin.clickconsumer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "click_counts",
        indexes = {@Index(name = "idx_count_key", columnList = "countKey", unique = true)})
public class ClickCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The key from our Kafka message - could be a userId or "global" for total counts.
     * We index this column because we'll frequently query by key for fast lookups.
     */
    @Column(name = "count_key", nullable = false, unique = true)
    private String countKey;

    /**
     * The actual count value - this gets updated every time we receive a new
     * processed result from our Kafka Streams application.
     */
    @Column(name = "count_value", nullable = false)
    private Long countValue;

    /**
     * Timestamp of when this record was last updated.
     * This helps us track the freshness of our data and debug timing issues.
     */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * Version field for optimistic locking.
     * This prevents race conditions when multiple threads try to update the same count.
     */
    @Version
    private Long version;

    public ClickCount() {}

    public ClickCount(String countKey, Long countValue) {
        this.countKey = countKey;
        this.countValue = countValue;
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateCount(Long newCount) {
        this.countValue = newCount;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCountKey() { return countKey; }
    public void setCountKey(String countKey) { this.countKey = countKey; }

    public Long getCountValue() { return countValue; }
    public void setCountValue(Long countValue) {
        this.countValue = countValue;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickCount that = (ClickCount) o;
        return Objects.equals(countKey, that.countKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countKey);
    }

    @Override
    public String toString() {
        return String.format("ClickCount{id=%d, countKey='%s', countValue=%d, lastUpdated=%s, version=%d}",
                id, countKey, countValue, lastUpdated, version);
    }
}
