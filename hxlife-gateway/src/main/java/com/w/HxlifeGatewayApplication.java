package com.w;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;



@SpringBootApplication
public class HxlifeGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HxlifeGatewayApplication.class, args);
    }

}
