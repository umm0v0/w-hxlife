package com.whx.lisener;

import com.whx.entity.Blog;
import com.whx.service.IBlogService;
import com.whx.utils.RabbitMqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.whx.utils.RabbitMqConstants.*;

@RequiredArgsConstructor
@Component
public class FeedLisener {
    private final IBlogService blogService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = FEED_QUEUE),
            exchange = @Exchange(name = FEED_EXCHANGE, durable = "true"),
            key = FEED_KEY
    ))
    public void feedToFans(Map<String,Object>map){
        //推送到关注该用户的收件箱
        blogService.feedToFans(map.get("blogId").toString(),map.get("userId").toString());

    }
}
