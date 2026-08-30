package com.smartretail.pricemonitor.microservices.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartretail.pricemonitor")
public class LocationMicroserviceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8082");
        SpringApplication.run(LocationMicroserviceApplication.class, args);
    }
}
