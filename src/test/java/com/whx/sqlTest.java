package com.whx;


import cn.hutool.core.collection.CollUtil;
import com.whx.entity.SeckillVoucher;
import com.whx.entity.Shop;
import com.whx.entity.User;
import com.whx.service.ISeckillVoucherService;
import com.whx.service.IShopService;
import com.whx.service.IUserService;
import com.whx.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest

public class sqlTest {
    @Autowired
    private IUserService userService;
    @Autowired
    private  ISeckillVoucherService seckillVoucherService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IShopService shopService;
    @Autowired
    private LocalVariableTableParameterNameDiscoverer localSpringDocParameterNameDiscoverer;

    @Test
    public void testSql() {
        List<User> list = userService.list();
        System.out.println(list);
    }
    @Test
    public void demo(){
        LocalDateTime localDateTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        System.out.println(localDateTime.toEpochSecond(ZoneOffset.UTC));


    }

    @Test
    public void hotSeckill(){
        List<SeckillVoucher> list = seckillVoucherService.list();
        Map<String, String> collect = list.stream().collect(Collectors.toMap(
                v -> RedisConstants.SECKILL_STOCK_KEY +v.getVoucherId().toString(),
                v -> v.getStock().toString()
        ));
        if(!CollUtil.isEmpty(collect)){
            stringRedisTemplate.opsForValue().multiSet(collect);
        }
    }
    @Test
    public void timeTamp(){
        System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    public void Geo() {
        List<Shop> shops = shopService.lambdaQuery().list();
        Map<Long,List<Shop>>map=shops.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        for(Map.Entry<Long,List<Shop>>entry:map.entrySet()){
            Long typeId = entry.getKey();
            String key=RedisConstants.SHOP_GEO_KEY+typeId;
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>>locations=new ArrayList<>();
            for(Shop shop:value){
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(),shop.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }
}