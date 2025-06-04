package com.merlin.clickstreams.health;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that monitors the state of our Kafka Streams application.
 * This integrates with Spring Boot Actuator to provide health information
 * that can be used by monitoring systems or load balancers.
 *
 * Monitoring stream health is crucial in production because it helps you
 * detect and respond to processing issues before they affect users.
 */
@Component
public class KafkaStreamsHealthIndicator implements HealthIndicator {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    public KafkaStreamsHealthIndicator(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    @Override
    public Health health() {
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

            if (kafkaStreams == null) {
                return Health.down()
                        .withDetail("error", "KafkaStreams instance is null")
                        .build();
            }

            KafkaStreams.State state = kafkaStreams.state();

            if (state == KafkaStreams.State.RUNNING) {
                return Health.up()
                        .withDetail("state", state.name())
                        .withDetail("message", "Kafka Streams is running normally")
                        .build();
            } else {
                return Health.down()
                        .withDetail("state", state.name())
                        .withDetail("message", "Kafka Streams is not in RUNNING state")
                        .build();
            }

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
