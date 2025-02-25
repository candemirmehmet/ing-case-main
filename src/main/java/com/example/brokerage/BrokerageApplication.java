package com.example.brokerage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.brokerage.entities")
public class BrokerageApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrokerageApplication.class, args);
    }
}
