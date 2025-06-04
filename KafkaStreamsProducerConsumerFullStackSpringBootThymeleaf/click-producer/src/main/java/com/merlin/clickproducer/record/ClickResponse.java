package com.merlin.clickproducer.record;

public record ClickResponse(boolean success, String message, long userClickCount, long totalClickCount, String userId) {
}