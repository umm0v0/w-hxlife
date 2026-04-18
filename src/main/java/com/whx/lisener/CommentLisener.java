package com.whx.lisener;

import com.whx.entity.BlogComments;
import com.whx.service.IBlogCommentsService;
import com.whx.utils.RabbitMqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor
@Slf4j
@Component
public class CommentLisener {

    private final IBlogCommentsService blogCommentsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMqConstants.BLOG_COMMENT_CACHE_QUEUE),
            exchange = @Exchange(name = RabbitMqConstants.BLOG_COMMENT_CACHE_EXCHANGE),
            key = RabbitMqConstants.BLOG_COMMENT_CACHE_KEY))
    public void updateCacheComment(BlogComments blogComments){
        //更新缓存
        log.info("进入更新缓存队列，评论id：{}",blogComments.getId());
        blogCommentsService.updateCommentCache(blogComments);
        log.info("更新缓存成功，评论id：{}",blogComments.getId());
    }
}
