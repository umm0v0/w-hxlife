package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.Result;
import com.w.entity.Follow;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long id, Boolean isFollow);

    Result commonFollows(Long id);
}
