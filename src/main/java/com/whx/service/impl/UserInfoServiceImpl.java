package com.whx.service.impl;

import com.whx.entity.UserInfo;
import com.whx.mapper.UserInfoMapper;
import com.whx.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
