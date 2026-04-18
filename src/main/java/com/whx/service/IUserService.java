package com.whx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whx.dto.LoginFormDTO;
import com.whx.dto.Result;
import com.whx.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone);

    Result login(LoginFormDTO loginFormDTO);

    Result sign();

    Result signCount();
}
