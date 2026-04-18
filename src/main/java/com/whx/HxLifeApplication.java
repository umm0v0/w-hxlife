package com.whx;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


//@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
@MapperScan("com.whx.mapper")
@SpringBootApplication
public class HxLifeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HxLifeApplication.class, args);
    }

}
