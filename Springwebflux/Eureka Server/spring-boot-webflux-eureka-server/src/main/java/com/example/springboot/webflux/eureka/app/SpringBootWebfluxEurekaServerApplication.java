package com.example.springboot.webflux.eureka.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class SpringBootWebfluxEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxEurekaServerApplication.class, args);
    }

}
