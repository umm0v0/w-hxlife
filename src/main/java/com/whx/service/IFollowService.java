package com.whx.service;

import com.whx.dto.Result;
import com.whx.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

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
