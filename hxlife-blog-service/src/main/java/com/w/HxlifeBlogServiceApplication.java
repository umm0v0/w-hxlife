package com.w;

import com.w.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.w.client",defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("com.w.mapper")
@SpringBootApplication
public class HxlifeBlogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HxlifeBlogServiceApplication.class, args);
    }

}
