package com.merlin.clickstreams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
@ComponentScan(basePackages = {"com.merlin.clickstreams", "com.merlin.kafka"})
public class ClickStreamsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClickStreamsApplication.class, args);
    }
}
