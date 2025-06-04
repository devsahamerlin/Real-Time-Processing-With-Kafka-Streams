package com.merlin.kafka.config;

import com.merlin.kafka.model.ClickCountResult;
import com.merlin.kafka.model.ClickEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    private final int numberOfPartitions = 1;
    /**
     * Creates Kafka topics automatically if they don't exist.
     * This is helpful for development but should be managed differently in production.
     */
    @Bean
    public NewTopic clicksTopic() {
        return new NewTopic(KafkaTopics.CLICKS_TOPIC, numberOfPartitions, (short) 1);
    }

    @Bean
    public NewTopic clickCountsTopic() {
        return new NewTopic(KafkaTopics.CLICK_COUNTS_TOPIC, numberOfPartitions, (short) 1);
    }

    /**
     * Producer configuration for sending ClickEvent messages.
     * JSON serialization makes the data human-readable and easy to debug.
     */
    @Bean
    public ProducerFactory<String, ClickEvent> clickEventProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // These settings improve reliability and performance
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ClickEvent> clickEventKafkaTemplate() {
        return new KafkaTemplate<>(clickEventProducerFactory());
    }

    /**
     * Consumer configuration for receiving ClickCountResult messages.
     * The consumer group ID is important for scaling and load distribution.
     */
    @Bean
    public ConsumerFactory<String, ClickCountResult> clickCountResultConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "click-count-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Configure JSON deserializer to trust our specific classes
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.merlin.kafka.model");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ClickCountResult.class.getName());

        // Start reading from the beginning of the topic for this exercise
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClickCountResult>
    clickCountResultKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ClickCountResult> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(clickCountResultConsumerFactory());
        return factory;
    }

    /**
     * Admin client for managing topics and cluster operations.
     * Useful for topic creation and cluster monitoring.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
