package com.smartretail.pricemonitor.microservices.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartretail.pricemonitor")
public class CatalogMicroserviceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8083");
        SpringApplication.run(CatalogMicroserviceApplication.class, args);
    }
}
