package com.merlin.clickconsumer.listener;

import com.merlin.clickconsumer.service.ClickCountService;
import com.merlin.kafka.config.KafkaTopics;
import com.merlin.kafka.model.ClickCountResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that consumes processed click count results.
 * This is where our event-driven architecture comes full circle.
 *
 * When our Kafka Streams application processes click events and produces
 * aggregated results, this listener receives those results and stores them
 * in our database for fast API access. This pattern allows us to have both
 * real-time stream processing AND fast query performance.
 */
@Component
public class KafkaClickCountListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClickCountListener.class);

    private final ClickCountService clickCountService;

    @Autowired
    public KafkaClickCountListener(ClickCountService clickCountService) {
        this.clickCountService = clickCountService;
    }

    /**
     * Listen for click count results from our Kafka Streams application.
     *
     * The @KafkaListener annotation automatically deserializes the JSON messages
     * into our ClickCountResult objects. Notice how we specify the topic name
     * using our centralized KafkaTopics constants - this prevents typos and
     * makes topic management easier.
     *
     * The containerFactory parameter refers to the configuration we set up
     * in our kafka-broker module, demonstrating how our modular approach
     * keeps configurations consistent across the entire system.
     */
    @KafkaListener(
            topics = KafkaTopics.CLICK_COUNTS_TOPIC,
            containerFactory = "clickCountResultKafkaListenerContainerFactory"
    )
    public void handleClickCountResult(ClickCountResult clickCountResult) {
        try {
            logger.info("Received click count result: {}", clickCountResult);

            String key = clickCountResult.getKey();
            Long count = clickCountResult.getCount();
            clickCountService.updateClickCount(key, count);

            logger.debug("Successfully processed click count result for key: {}", key);

        } catch (Exception e) {
            logger.error("Error processing click count result: {}", clickCountResult, e);
        }
    }
}
