package com.w;

import com.w.config.DefaultFeignConfig;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.w.mapper")
@EnableFeignClients(basePackages = "com.w.client",defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class HxlifeUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HxlifeUserServiceApplication.class, args);
    }

}
