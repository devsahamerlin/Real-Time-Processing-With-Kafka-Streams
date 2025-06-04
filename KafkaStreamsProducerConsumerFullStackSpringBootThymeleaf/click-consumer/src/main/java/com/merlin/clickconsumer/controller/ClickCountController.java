package com.merlin.clickconsumer.controller;

import com.merlin.clickconsumer.entity.ClickCount;
import com.merlin.clickconsumer.service.ClickCountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller that exposes our click count data through HTTP endpoints.
 * This controller completes our event-driven architecture by making the
 * processed results available to external consumers.
 *
 * Notice how this controller focuses purely on HTTP concerns - it delegates
 * all business logic to the service layer. This separation of concerns makes
 * our code more maintainable and testable.
 */
@RestController
@RequestMapping("/api/clicks")
@CrossOrigin(origins = "*") // Allow cross-origin requests for web frontend integration
public class ClickCountController {

    private static final Logger logger = LoggerFactory.getLogger(ClickCountController.class);

    private final ClickCountService clickCountService;

    @Autowired
    public ClickCountController(ClickCountService clickCountService) {
        this.clickCountService = clickCountService;
    }

    /**
     * GET /api/clicks/count - Get the global click count
     * This is the main endpoint requested in the exercise requirements.
     *
     * Returns the total number of clicks across all users in real-time.
     * The data comes from our Kafka Streams processing, so it reflects
     * the most up-to-date aggregated results.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getGlobalClickCount() {
        try {
            Long globalCount = clickCountService.getGlobalClickCount().orElse(0L);

            Map<String, Object> response = Map.of(
                    "totalClicks", globalCount,
                    "timestamp", System.currentTimeMillis(),
                    "message", "Global click count retrieved successfully"
            );

            logger.debug("Retrieved global click count: {}", globalCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving global click count", e);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Failed to retrieve click count",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * GET /api/clicks/user/{userId} - Get click count for a specific user
     * This endpoint allows you to query individual user statistics.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserClickCount(@PathVariable String userId) {
        try {
            Long userCount = clickCountService.getUserClickCount(userId);

            Map<String, Object> response = Map.of(
                    "userId", userId,
                    "clickCount", userCount,
                    "timestamp", System.currentTimeMillis()
            );

            logger.debug("Retrieved click count for user '{}': {}", userId, userCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving click count for user: {}", userId, e);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Failed to retrieve user click count",
                    "userId", userId,
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * GET /api/clicks/leaderboard - Get top users by click count
     * This endpoint creates a leaderboard of the most active clickers.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ClickCount> topUsers = clickCountService.getTopUsers(limit);

            // Transform the data for better JSON response structure
            List<Map<String, Object>> leaderboard = topUsers.stream()
                    .map(clickCount -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("userId", clickCount.getCountKey());
                        map.put("clickCount", clickCount.getCountValue());
                        map.put("lastUpdated", clickCount.getLastUpdated().toString());
                        return map;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("leaderboard", leaderboard);
            response.put("limit", limit);
            response.put("totalUsers", leaderboard.size());
            response.put("timestamp", System.currentTimeMillis());

            logger.debug("Retrieved leaderboard with {} users", leaderboard.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving leaderboard", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve leaderboard");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * GET /api/clicks/stats - Get comprehensive statistics
     * This endpoint provides a complete overview of the clicking activity.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Long globalCount = clickCountService.getGlobalClickCount().orElse(0L);
            List<ClickCount> allUsers = clickCountService.getAllUserClickCounts();

            Map<String, Object> response = Map.of(
                    "totalClicks", globalCount,
                    "totalUsers", allUsers.size(),
                    "averageClicksPerUser", allUsers.isEmpty() ? 0.0 :
                            allUsers.stream().mapToLong(ClickCount::getCountValue).average().orElse(0.0),
                    "timestamp", System.currentTimeMillis()
            );

            logger.debug("Retrieved comprehensive statistics");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving statistics", e);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Failed to retrieve statistics",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
