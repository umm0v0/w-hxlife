package com.w.utils;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Component
public class RedisIdWorker {
    private static final Long BEGIN_TIMESTAMP=1767225600L;
    private static final int COUNT_BIT=32;
    private  final StringRedisTemplate stringRedisTemplate;
    public long nextId(String prefix){
        LocalDateTime localDateTime=LocalDateTime.now();
        long epochSecond = localDateTime.toEpochSecond(ZoneOffset.UTC);
        //String epoch=String.valueOf(epochSecond);
        long diff=epochSecond-BEGIN_TIMESTAMP;
        String date=localDateTime.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key="icr:"+prefix+":"+date;
        long count = stringRedisTemplate.opsForValue().increment(key);
        return diff<<COUNT_BIT | count;


    }
}
