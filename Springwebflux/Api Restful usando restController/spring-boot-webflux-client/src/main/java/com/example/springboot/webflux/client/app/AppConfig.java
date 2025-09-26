package com.example.springboot.webflux.client.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${config.base.endpoint}")
    private String endpoint;
    @Bean
    @LoadBalanced
    public WebClient.Builder registrarWebClient(){
        return WebClient.builder().baseUrl(endpoint);
    }
}
