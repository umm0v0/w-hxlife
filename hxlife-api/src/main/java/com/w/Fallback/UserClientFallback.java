package com.w.Fallback;

import com.w.client.UserClient;
import com.w.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;


@Component
public class UserClientFallback implements UserClient {
    @Override
    public User getById(Long userId) {
        return new User();
    }

    @Override
    public List<User> list(Collection<Long> ids) {
        return List.of();
    }
}
