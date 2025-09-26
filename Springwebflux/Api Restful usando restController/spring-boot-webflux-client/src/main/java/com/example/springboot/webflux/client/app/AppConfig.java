package com.example.springboot.webflux.client.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${config.base.endpoint}")
    private String endpoint;
    @Bean
    public WebClient registrarWebClient(){
        return WebClient.create(endpoint);
    }
}
