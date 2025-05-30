package com.merlin.app.config;
import org.apache.kafka.common.serialization.Serdes;
import java.util.Properties;

public class KafkaConfig {

    public Properties getKafkaProperties() {
        Properties props = new Properties();
        props.put("application.id", "Kafka-Streams-Weather-Processor-Application");
        props.put("bootstrap.servers", "localhost:9092");
        props.put("default.key.serde", Serdes.String().getClass());
        props.put("default.value.serde", Serdes.String().getClass());
        props.put("auto.offset.reset", "earliest");
        return props;
    }

}
