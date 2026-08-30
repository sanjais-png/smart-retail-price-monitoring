package com.smartretail.microservices.agmarknet;

import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgmarknetEurekaClientConfig {

    @Bean
    public TransportClientFactories transportClientFactories() {
        return new Jersey3TransportClientFactories();
    }
}
