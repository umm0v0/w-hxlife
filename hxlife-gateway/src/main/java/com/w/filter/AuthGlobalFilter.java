package com.w.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.w.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher=new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //判断是否有排除的路径
        if(isExclude(request.getPath().toString())){
            return  chain.filter(exchange);
        }
        //如果没有那就是传统操作
        String token=null;
        List<String> authorization = request.getHeaders().get("authorization");
        if(CollUtil.isEmpty(authorization)){
            response.setRawStatusCode(401);
            return response.setComplete();
        }
        //如果有，需要把登录信息塞进请求头
        token=authorization.get(0);
        String key="login:token:"+token;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        String userId = entries.get("id").toString();

        //如果token过期
        if(StrUtil.isBlank(userId)){
            response.setRawStatusCode(401);
            return response.setComplete();
        }
        //把userId塞进请求头
        request.mutate().header("user-info",userId).build();
        return chain.filter(exchange);




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
