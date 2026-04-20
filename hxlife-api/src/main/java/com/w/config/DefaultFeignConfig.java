package com.w.config;

import com.w.utils.UserHolder;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DefaultFeignConfig{
    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            Long userId= UserHolder.getUser().getId();
            if(userId==null){
                return;
            }
            requestTemplate.header("user-info",userId.toString());
        };
    }

    @Bean
    public Logger.Level loggerLevel(){
        //TODO:看下日志，防止出错，之后看情况删
        return Logger.Level.FULL;
    }
}
