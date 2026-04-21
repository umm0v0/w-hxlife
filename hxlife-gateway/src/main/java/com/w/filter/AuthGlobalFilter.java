package com.w.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.w.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(0)
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher=new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path=request.getPath().toString();
        boolean isPath=isExclude(path);
        String token=request.getHeaders().getFirst("Authorization");
        if(StrUtil.isBlank(token)){
            if(isPath){
                return chain.filter(exchange);
            }
            return  unauthorized(exchange);
        }
        //如果有token的话
        String key="login:token:"+token;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        //判断是否过期
        if(entries==null||entries.isEmpty()){
            //token过期
            return unauthorized(exchange);
        }
        //token有效
        stringRedisTemplate.expire(key,30L,TimeUnit.MINUTES);
        String userId=entries.get("id").toString();
        return chain.filter(
                exchange.mutate().
                        request(builder -> builder.header("user-info",userId)).
                        build()
        );



    }
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setRawStatusCode(401);
        return response.setComplete();
    }

    private boolean isExclude(String path) {
        for(String exclude:authProperties.getExcludePaths()){
            if (antPathMatcher.match(exclude,path)){
                return true;
            }
        }
        return false;
    }
}
