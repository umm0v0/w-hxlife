package com.w.interception;

import cn.hutool.core.util.StrUtil;
import com.w.dto.UserDTO;
import com.w.utils.UserHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class UserInfoInterception implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("user-info");
        if(!StrUtil.isBlank(header)){
            UserDTO userDTO=new UserDTO();
            userDTO.setId(Long.parseLong(header));
            UserHolder.saveUser(userDTO);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
