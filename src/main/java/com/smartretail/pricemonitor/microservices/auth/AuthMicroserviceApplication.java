package com.smartretail.pricemonitor.microservices.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartretail.pricemonitor")
public class AuthMicroserviceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        SpringApplication.run(AuthMicroserviceApplication.class, args);
    }
}
