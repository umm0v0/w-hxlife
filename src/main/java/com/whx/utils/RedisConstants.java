package com.whx.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;


    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_NULL_SHOP_TTL=2L;

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
    private static final String LOCK_USER_KEY = "lock:user:";

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
    public static final String USER_FOLLOW_KEY = "follow:";

    //博客
    public static final String BLOG_COMMENTS_ROOTID_KEY = "blog:comments:root:";
    public static final String BLOG_COMMENTS_INFO_KEY = "blog:comments:info:";
    public static final String BLOG_COMMENTS_COUNT_KEY = "blog:comments:count:";
    public static final Long CACHE_BLOG_COMMENTS_COUNT_TTL = 30L;
}
