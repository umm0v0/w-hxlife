package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.Result;
import com.w.entity.BlogComments;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IBlogCommentsService extends IService<BlogComments> {

    Result saveComment(BlogComments blogComments);

    void updateCommentCache(BlogComments blogComments);

    Result queryCommentList(Long blogId, Long minTime, Long offset);

    Result queryReplyList(Long commentId);
}
