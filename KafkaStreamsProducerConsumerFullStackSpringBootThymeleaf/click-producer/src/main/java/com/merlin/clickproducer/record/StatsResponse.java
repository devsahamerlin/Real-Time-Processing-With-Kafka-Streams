package com.merlin.clickproducer.record;

public record StatsResponse(long userClickCount, long totalClickCount, String userId) {
}
