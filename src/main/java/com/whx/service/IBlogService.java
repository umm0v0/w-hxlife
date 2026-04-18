package com.whx.service;

import com.whx.dto.Result;
import com.whx.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IBlogService extends IService<Blog> {

    Result saveBlog(Blog blog);


    Result feedToFans(String blogId,String userId);

    Result queryFollowBlog(Long lastTime, Long offset);

    Result likeBlog(Long id);
}
