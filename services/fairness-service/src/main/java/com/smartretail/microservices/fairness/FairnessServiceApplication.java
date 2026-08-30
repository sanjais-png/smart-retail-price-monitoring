package com.smartretail.microservices.fairness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.smartretail")
@EnableDiscoveryClient
public class FairnessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FairnessServiceApplication.class, args);
    }
}
