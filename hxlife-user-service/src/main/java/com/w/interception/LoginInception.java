package com.w.interception;

import com.w.dto.UserDTO;
import com.w.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//@Component
public class LoginInception implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //查询有没有用户，前面是一个刷新拦截器，如果有用户的话，说明token有东西
        //System.out.println("这是登录拦截器");
        UserDTO user = UserHolder.getUser();
        if(user==null){
            //登录失败
            System.out.println("被拦截的路径是："+request.getRequestURI());
            response.setStatus(401);
            return false;
        }
        return true;
    }


}
