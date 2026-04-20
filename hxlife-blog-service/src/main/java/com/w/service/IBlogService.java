package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.Result;
import com.w.entity.Blog;

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
