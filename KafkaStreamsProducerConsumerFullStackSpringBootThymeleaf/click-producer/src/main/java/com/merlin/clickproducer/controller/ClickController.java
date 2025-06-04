package com.merlin.clickproducer.controller;

import com.merlin.clickproducer.entity.UserClick;
import com.merlin.clickproducer.record.ClickResponse;
import com.merlin.clickproducer.record.StatsResponse;
import com.merlin.clickproducer.service.ClickService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

/**
 * Web controller that handles HTTP requests and coordinates with our service layer.
 * This controller demonstrates how to build a responsive web interface that
 * integrates with our event-driven backend. Notice how we separate the concerns:
 * the controller handles HTTP, the service handles business logic, and Kafka handles events.
 */
@Controller
public class ClickController {

    private static final Logger logger = LoggerFactory.getLogger(ClickController.class);

    private final ClickService clickService;

    @Autowired
    public ClickController(ClickService clickService) {
        this.clickService = clickService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        // Generate or retrieve user ID from session
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
            session.setAttribute("userId", userId);
            logger.info("Created new user session: {}", userId);
        }

        // Add user information and statistics to the model
        model.addAttribute("userId", userId);
        model.addAttribute("userClickCount", clickService.getUserClickCount(userId));
        model.addAttribute("totalClickCount", clickService.getTotalClickCount());

        return "index.html";
    }

    @PostMapping("/api/click")
    @ResponseBody
    public ClickResponse processClick(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String sessionId = session.getId();

        try {
            clickService.processClick(userId, sessionId);

            long userCount = clickService.getUserClickCount(userId);
            long totalCount = clickService.getTotalClickCount();

            logger.info("Processed click for user: {}, new count: {}", userId, userCount);

            return new ClickResponse(true, "Click processed successfully",
                    userCount, totalCount, userId);

        } catch (Exception e) {
            logger.error("Error processing click for user: {}", userId, e);
            return new ClickResponse(false, "Failed to process click",
                    0L, 0L, userId);
        }
    }

    @GetMapping("/api/history/{userId}")
    @ResponseBody
    public List<UserClick> getUserHistory(@PathVariable String userId) {
        return clickService.getUserClickHistory(userId);
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public StatsResponse getStats(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return new StatsResponse(
                clickService.getUserClickCount(userId),
                clickService.getTotalClickCount(),
                userId
        );
    }
}
