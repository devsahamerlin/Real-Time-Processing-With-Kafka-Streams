package com.merlin.clickstreams.processor;

import com.merlin.kafka.config.KafkaTopics;
import com.merlin.kafka.model.ClickCountResult;
import com.merlin.kafka.model.ClickEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This is where the real-time magic happens! This class defines how we process
 * the stream of click events. Think of it as a set of instructions that tells
 * Kafka Streams: "When you see click events, here's how to count them and
 * what to do with the results."
 *
 * The beauty of Kafka Streams is that it handles all the complexity of
 * distributed processing, fault tolerance, and scaling for us. We just
 * need to define the business logic.
 */
@Component
public class ClickStreamProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ClickStreamProcessor.class);

    /**
     * This method is automatically called by Spring when the application starts.
     * The StreamsBuilder is like a blueprint for our data processing pipeline.
     * We define the transformations we want, and Kafka Streams executes them
     * across our data as it flows through the system.
     */
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        logger.info("Building Kafka Streams processing pipeline...");

        // Create JSON serdes (serializers/deserializers) for our custom objects
        // Serdes tell Kafka Streams how to convert between Java objects and bytes
        JsonSerde<ClickEvent> clickEventSerde = new JsonSerde<>(ClickEvent.class);
        JsonSerde<ClickCountResult> clickCountResultSerde = new JsonSerde<>(ClickCountResult.class);

        // Create a stream from our clicks topic
        // This is like opening a continuous data pipeline from the topic
        KStream<String, ClickEvent> clickStream = streamsBuilder
                .stream(KafkaTopics.CLICKS_TOPIC,
                        Consumed.with(Serdes.String(), clickEventSerde));

        // Let's log each click event as it flows through for debugging
        clickStream.peek((key, value) ->
                logger.info("Processing click event - Key: {}, Value: {}", key, value));

        // Process clicks in multiple ways to demonstrate different counting strategies

        // 1. Count clicks per user (individual user analytics)
        processUserClickCounts(clickStream, clickCountResultSerde);

        // 2. Count total clicks globally (overall system analytics)
        processGlobalClickCounts(clickStream, clickCountResultSerde);

        // 3. Count clicks in time windows (trending analytics)
        processWindowedClickCounts(clickStream, clickCountResultSerde);

        logger.info("Kafka Streams processing pipeline built successfully!");
    }

    /**
     * Count clicks per individual user.
     * This creates a running count of how many times each user has clicked.
     * The groupByKey() operation ensures all clicks from the same user
     * are processed together, maintaining consistency.
     */
    private void processUserClickCounts(KStream<String, ClickEvent> clickStream,
                                        JsonSerde<ClickCountResult> resultSerde) {

        clickStream
                // Group by user ID (the key) - this ensures all events for the same user
                // are processed by the same instance, which is crucial for accurate counting
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(ClickEvent.class)))

                // Count the events for each user
                // This creates a KTable (think of it as a continuously updated database table)
                // where each user ID maps to their current click count
                .count(Materialized.<String, Long>as(
                                Stores.persistentKeyValueStore("user-click-counts-store"))
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))

                // Convert the count to our result format
                // The mapValues operation transforms each count into a ClickCountResult object
                .mapValues((userId, count) -> new ClickCountResult(
                        userId,
                        count,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ), Materialized.with(Serdes.String(), resultSerde))

                // Convert back to a stream so we can send to output topic
                .toStream()

                // Log the results for monitoring
                .peek((userId, result) ->
                        logger.info("User click count updated - User: {}, Count: {}",
                                userId, result.getCount()))

                // Send results to the output topic where consumers can read them
                .to(KafkaTopics.CLICK_COUNTS_TOPIC,
                        Produced.with(Serdes.String(), resultSerde));
    }

    /**
     * Count total clicks across all users.
     * This demonstrates how to create global statistics from individual events.
     * We repartition all events to a single key so they can be counted together.
     */
    private void processGlobalClickCounts(KStream<String, ClickEvent> clickStream,
                                          JsonSerde<ClickCountResult> resultSerde) {

        clickStream
                // Repartition all events to use the same key ("global")
                // This means all events will be processed by the same partition,
                // allowing us to maintain a single global counter
                .selectKey((userId, clickEvent) -> "global")

                // Group by the new key
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(ClickEvent.class)))

                // Count all events (now they all have the same key)
                .count(Materialized.<String, Long>as(
                                Stores.persistentKeyValueStore("global-click-counts-store"))
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))

                // Transform to our result format
                .mapValues((key, count) -> new ClickCountResult(
                        "global",
                        count,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ), Materialized.with(Serdes.String(), resultSerde))

                .toStream()

                .peek((key, result) ->
                        logger.info("Global click count updated - Count: {}", result.getCount()))

                // Send to output topic with "global" as the key
                .to(KafkaTopics.CLICK_COUNTS_TOPIC,
                        Produced.with(Serdes.String(), resultSerde));
    }

    /**
     * Count clicks in time windows for trending analysis.
     * This creates counts for recent time periods, which is useful for
     * understanding usage patterns and detecting spikes in activity.
     *
     * Time windows are one of the most powerful features of Kafka Streams
     * because they let us analyze data over time periods automatically.
     */
    private void processWindowedClickCounts(KStream<String, ClickEvent> clickStream,
                                            JsonSerde<ClickCountResult> resultSerde) {

        clickStream
                // Group by user for windowed counting
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(ClickEvent.class)))

                // Create 1-minute tumbling windows
                // Tumbling windows are fixed-size, non-overlapping time periods
                // Each click event falls into exactly one window based on its timestamp
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))

                // Count clicks within each window
                .count(Materialized.<String, Long>as(
                                Stores.persistentWindowStore("windowed-click-counts-store",
                                        Duration.ofMinutes(10), // retention period
                                        Duration.ofMinutes(1),  // window size
                                        false))                 // retain duplicates = false
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))

                // Convert windowed results back to a stream
                .toStream()

                // Transform the windowed key-value pairs into our result format
                // The key now includes both the user ID and the time window information
                .map((windowedKey, count) -> {
                    String userId = windowedKey.key();
                    long windowStart = windowedKey.window().start();
                    long windowEnd = windowedKey.window().end();

                    String windowKey = String.format("%s-window-%d-%d", userId, windowStart, windowEnd);
                    ClickCountResult result = new ClickCountResult(
                            windowKey,
                            count,
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    );

                    return KeyValue.pair(windowKey, result);
                })

                .peek((windowKey, result) ->
                        logger.info("Windowed click count - Window: {}, Count: {}",
                                windowKey, result.getCount()))

                // Send windowed results to the same output topic
                // Consumers can distinguish between different types of counts by the key format
                .to(KafkaTopics.CLICK_COUNTS_TOPIC,
                        Produced.with(Serdes.String(), resultSerde));
    }
}
