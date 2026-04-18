package com.whx.service;

import com.whx.dto.Result;
import com.whx.entity.BlogComments;
import com.baomidou.mybatisplus.extension.service.IService;

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
