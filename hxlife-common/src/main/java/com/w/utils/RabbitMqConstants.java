package com.w.utils;

public class RabbitMqConstants {
    //交换机
    public static final String SECKILL_EXCHANGE="seckill.direct";
    public static final String SECKILL_DEAD_EXCHANGE="seckill.dead.exchange";
    public static final String SECKILL_DELAY_EXCHANGE="seckill.delay.exchange";
    public static final String FEED_EXCHANGE="feed.direct";
    public static final String BLOG_COMMENT_CACHE_EXCHANGE="blog.comment.cache.exchange";
    //队列
    public static final String SECKILL_QUEUE="seckill.queue";
    public static final String SECKILL_DELAY_QUEUE="seckill.delay.queue";
    public static final String SECKILL_DEAD_QUEUE="seckill.dead.queue";
    public static final String FEED_QUEUE="feed.queue";
    public static final String BLOG_COMMENT_CACHE_QUEUE="blog.comment.cache.queue";

    //routing_key
    public static final String SECKILL_ORDER_KEY="seckill.order";
    public static final String SECKILL_DELAY_KEY="seckill.delay";
    public static final String SECKILL_DEAD_KEY="seckill.dead";
    public static final String FEED_KEY="feed";
    public static final String BLOG_COMMENT_CACHE_KEY="blog.comment.cache";
}
