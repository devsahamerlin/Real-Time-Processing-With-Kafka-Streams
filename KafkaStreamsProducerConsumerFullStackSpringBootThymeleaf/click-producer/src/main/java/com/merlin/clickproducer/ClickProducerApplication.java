package com.merlin.clickproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.merlin.clickproducer", "com.merlin.kafka"})
public class ClickProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClickProducerApplication.class, args);
    }
}