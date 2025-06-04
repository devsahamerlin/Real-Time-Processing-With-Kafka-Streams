package com.merlin.clickstreams.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.apache.kafka.streams.StreamsConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Kafka Streams.
 * This class sets up the fundamental properties that control how our
 * streams application behaves. Think of this as the "settings" for our
 * real-time processing engine.
 */
@Configuration
public class ClickStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.application.name:click-streams-app}")
    private String applicationName;

    /**
     * This bean provides the core configuration for Kafka Streams.
     * The configuration determines important behaviors like how the application
     * identifies itself to Kafka, how it handles serialization, and
     * how it manages its processing state.
     */
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();

        // Basic connection settings
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Default serialization settings
        // These are used when we don't explicitly specify serdes in our topology
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Processing settings for better performance and reliability
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000); // Commit every second
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024); // 10MB cache

        // Consumer settings for the underlying Kafka consumers
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        // State store settings for better performance
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams-" + applicationName);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2); // Use 2 processing threads

        return new KafkaStreamsConfiguration(props);
    }
}
