package com.merlin.clickconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@ComponentScan(basePackages = {"com.merlin.clickconsumer", "com.merlin.kafka"})
public class ClickConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClickConsumerApplication.class, args);
    }
}
