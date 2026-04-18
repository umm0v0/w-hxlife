package com.whx;


import cn.hutool.json.JSONUtil;
import com.whx.entity.Shop;
import com.whx.service.IShopService;
import com.whx.utils.RedisConstants;
import com.whx.utils.RedisData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

@SpringBootTest
public class preRedisData {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IShopService shopService;
    @Test
    public void preData(){
        Shop one = shopService.lambdaQuery().eq(Shop::getId, 2L).one();
        RedisData redisData=new RedisData();
        redisData.setData(one);
        //快速过期一下
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(RedisConstants.CACHE_NULL_TTL));
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+2L, JSONUtil.toJsonStr(redisData));


    }
}
