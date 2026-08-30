package com.smartretail.pricemonitor.microservices.fairness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartretail.pricemonitor")
public class FairnessMicroserviceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8084");
        SpringApplication.run(FairnessMicroserviceApplication.class, args);
    }
}
