package com.smartretail.pricemonitor.microservices.agmarknet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smartretail.pricemonitor")
@EnableScheduling
public class AgmarknetSyncMicroserviceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8085");
        SpringApplication.run(AgmarknetSyncMicroserviceApplication.class, args);
    }
}
