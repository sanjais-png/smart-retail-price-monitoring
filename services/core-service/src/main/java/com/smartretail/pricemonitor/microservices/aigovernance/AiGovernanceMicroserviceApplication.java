package com.smartretail.pricemonitor.microservices.aigovernance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartretail.pricemonitor")
public class AiGovernanceMicroserviceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8086");
        SpringApplication.run(AiGovernanceMicroserviceApplication.class, args);
    }
}
