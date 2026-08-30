package com.smartretail.microservices.fairness;

import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FairnessEurekaClientConfig {

    @Bean
    public TransportClientFactories transportClientFactories() {
        return new Jersey3TransportClientFactories();
    }
}
