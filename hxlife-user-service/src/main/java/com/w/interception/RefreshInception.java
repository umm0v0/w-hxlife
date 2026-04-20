package com.w.interception;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.w.dto.UserDTO;
import com.w.utils.RedisConstants;
import com.w.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//@Component
@RequiredArgsConstructor
public class RefreshInception implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //校验是否有token
        //System.out.println("这是刷新拦截器");
        String token = request.getHeader("authorization");
        if(StrUtil.isBlankIfStr(token)){
            //直接放心，交给下面那个拦截器拦下来
            return true;
        }
        String key=RedisConstants.LOGIN_USER_KEY+token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //更新日期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        if(userMap==null||userMap.isEmpty()){
            //token过期
            return true;
        }
        UserDTO userDTO= BeanUtil.fillBeanWithMap(userMap,new  UserDTO(),true);
        UserHolder.saveUser(userDTO);
        return true;


    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
