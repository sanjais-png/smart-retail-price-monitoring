package com.smartretail.microservices.agmarknet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smartretail")
@EnableScheduling
@EnableDiscoveryClient
public class AgmarknetSyncServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgmarknetSyncServiceApplication.class, args);
    }
}
