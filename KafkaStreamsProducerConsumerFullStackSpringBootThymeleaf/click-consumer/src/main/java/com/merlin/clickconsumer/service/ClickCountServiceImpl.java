package com.merlin.clickconsumer.service;

import com.merlin.clickconsumer.entity.ClickCount;
import com.merlin.clickconsumer.repository.ClickCountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of our ClickCountService.
 * This class handles all the business logic for managing click count data.
 *
 * Notice how we use transactions to ensure data consistency, and how we handle
 * both insert and update scenarios gracefully. This service is designed to be
 * thread-safe and handle high-frequency updates from Kafka.
 */
@Service
@Transactional
public class ClickCountServiceImpl implements ClickCountService {

    private static final Logger logger = LoggerFactory.getLogger(ClickCountServiceImpl.class);

    private final ClickCountRepository clickCountRepository;

    @Autowired
    public ClickCountServiceImpl(ClickCountRepository clickCountRepository) {
        this.clickCountRepository = clickCountRepository;
    }

    @Override
    public void updateClickCount(String key, Long count) {
        try {
            Optional<ClickCount> existingCount = clickCountRepository.findByCountKey(key);

            if (existingCount.isPresent()) {
                ClickCount clickCount = existingCount.get();
                clickCount.updateCount(count);
                clickCountRepository.save(clickCount);
                logger.debug("Updated click count for key '{}': {}", key, count);
            } else {
                ClickCount newClickCount = new ClickCount(key, count);
                clickCountRepository.save(newClickCount);
                logger.info("Created new click count for key '{}': {}", key, count);
            }
        } catch (Exception e) {
            logger.error("Error updating click count for key '{}': {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to update click count", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClickCount> getClickCount(String key) {
        return clickCountRepository.findByCountKey(key);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getGlobalClickCount() {
        return clickCountRepository.findGlobalCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClickCount> getAllUserClickCounts() {
        return clickCountRepository.findAllUserCounts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClickCount> getTopUsers(int limit) {
        return clickCountRepository.findTopUsersByClickCount(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserClickCount(String userId) {
        return clickCountRepository.findByCountKey(userId)
                .map(ClickCount::getCountValue)
                .orElse(0L);
    }
}
