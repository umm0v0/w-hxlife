package com.w.config;

import com.w.interception.LoginInception;
import com.w.interception.RefreshInception;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


//@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RefreshInception refreshInception;
    @Resource
    private LoginInception loginInception;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(refreshInception).addPathPatterns("/**").order(0);
        registry.addInterceptor(loginInception)
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/shop/**",
                        "/shop-type/**",
                        "/voucher/**",
                        "/upload/**",
                        "/blog/hot",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/doc.html",
                        "/error"
                )
                .addPathPatterns("/**")
                .order(1);

    }
}
