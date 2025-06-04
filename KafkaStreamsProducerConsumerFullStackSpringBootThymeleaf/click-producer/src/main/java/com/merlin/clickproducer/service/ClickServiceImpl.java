package com.merlin.clickproducer.service;

import com.merlin.clickproducer.entity.UserClick;
import com.merlin.clickproducer.repository.UserClickRepository;
import com.merlin.kafka.config.KafkaTopics;
import com.merlin.kafka.model.ClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ClickServiceImpl implements ClickService {

    private static final Logger logger = LoggerFactory.getLogger(ClickServiceImpl.class);

    private final UserClickRepository userClickRepository;
    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    @Autowired
    public ClickServiceImpl(UserClickRepository userClickRepository,
                            KafkaTemplate<String, ClickEvent> kafkaTemplate) {
        this.userClickRepository = userClickRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void processClick(String userId, String sessionId) {
        LocalDateTime now = LocalDateTime.now();

        try {
            UserClick userClick = new UserClick(userId, now, sessionId);
            userClickRepository.save(userClick);
            logger.info("Saved click to database: {}", userClick);

            // Then, publish to Kafka for stream processing
            // Notice how we use the userId as the key - this ensures all clicks
            // from the same user go to the same partition, maintaining order
            ClickEvent clickEvent = ClickEvent.createClick(userId);
            kafkaTemplate.send(KafkaTopics.CLICKS_TOPIC, userId, clickEvent)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Failed to send click event to Kafka: {}", clickEvent, throwable);
                        } else {
                            logger.info("Successfully sent click event to Kafka: {}", clickEvent);
                        }
                    });

        } catch (Exception e) {
            logger.error("Error processing click for user: {}", userId, e);
            throw new RuntimeException("Failed to process click", e);
        }
    }

    @Override
    public List<UserClick> getUserClickHistory(String userId) {
        return userClickRepository.findByUserIdOrderByClickTimestampDesc(userId);
    }

    @Override
    public long getUserClickCount(String userId) {
        return userClickRepository.countByUserId(userId);
    }

    @Override
    public long getTotalClickCount() {
        return userClickRepository.countTotalClicks();
    }
}
