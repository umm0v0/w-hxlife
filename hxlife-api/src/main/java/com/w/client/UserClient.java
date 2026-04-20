package com.w.client;


import com.w.Fallback.UserClientFallback;
import com.w.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "hxlife-user-service",fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/user/{userId}")
    User getById(@PathVariable("userId") Long userId);

    @GetMapping("/user/list")
    List<User> list(@RequestParam("ids") Collection<Long> ids);
}
