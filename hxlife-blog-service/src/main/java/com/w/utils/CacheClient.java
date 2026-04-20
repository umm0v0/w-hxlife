package com.w.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public <R,ID> R getByIdWithPassThough(String prefix, ID id, Class<R> type, Function<ID,R>dbFind,Long ttl,TimeUnit timeUnit){
        String key=prefix+id;
        String s = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(s)){
            return JSONUtil.toBean(s,type);
        }
        if(s!=null){
            //缓存空值
            return null;
        }
        //去数据库里面进项查询
        R r = dbFind.apply(id);
        if(r==null){
            stringRedisTemplate.opsForValue().set(key,"", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(r),
                ttl,timeUnit);
        return r;
    }


    public <R,ID> R getByIdWithMutex(String prefix,ID id,Class<R>type ,Function<ID,R>dbFind,Long ttl,TimeUnit timeUnit) {
        String key =prefix+id;
        R r=null;
        String lockKey=RedisConstants.LOCK_SHOP_KEY+id;
        while(true){
            String s = stringRedisTemplate.opsForValue().get(key);
            if(StrUtil.isNotBlank(s)){
                return JSONUtil.toBean(s,type);
            }
            if(s!=null){
                //缓存空值
                return null;
            }


            //缓存里面没有数据，加锁，防止全打数据库里面去了,防止一起设置缓存,只让一个线程去做这件事


            boolean isSuccess=tryLock(lockKey);
            if(isSuccess){
                //只有拿到了锁的，才可以释放，之前的代码try画太大了，没拿到锁的把锁给放了
                try {

                    //二次检查，因为有些线程等在这里，不需要重新再查库了
                    s = stringRedisTemplate.opsForValue().get(key);
                    if(StrUtil.isNotBlank(s)){
                        //log.info("已经建立上缓存了，回去吧");
                        return JSONUtil.toBean(s,type);
                    }
                    if(s!=null){
                        //缓存空值
                        return null;
                    }

                    //log.info("获取库存，加上缓存");
                    r=dbFind.apply(id);
                    if(r==null){
                        stringRedisTemplate.opsForValue().set(key,"", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                        return null;
                    }
                    stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(r),ttl,timeUnit);
                }finally {
                    unLock(lockKey);
                }
            }else{
                //其他线程等一会再拿
                //log.error("其他线程等一会，有个线程已经拿到了锁");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }




    }

    private static final ExecutorService BUILD_CACHE= Executors.newSingleThreadExecutor();
    public <R,ID> R getByIdWithLogicalExpire(String prefix,ID id,Class<R>type,Function<ID,R>dbFind,Long ttl,TimeUnit timeUnit){
        String key=prefix+id;
        String s = stringRedisTemplate.opsForValue().get(key);
        //逻辑过期默认认为缓存里面是做了预热的，能用到缓存过期，要求相当高了，不可能这么高的要求还不做预热
        if(StrUtil.isBlank(s)){
            return null;
        }
        RedisData redisData = JSONUtil.toBean(s, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        R r = BeanUtil.copyProperties(redisData.getData(), type);
        if(expireTime.isAfter(LocalDateTime.now())){
            //还没有过期

            return r;
        }
        //过期，把任务交给一个线程完成即可
        String lockKey=RedisConstants.LOCK_SHOP_KEY+id;
        boolean isLock = tryLock(lockKey);

        if(isLock){

            //一样的，二次检查，防止有线程卡在入口
            String s1 = stringRedisTemplate.opsForValue().get(key);
            RedisData redisData2 = JSONUtil.toBean(s1, RedisData.class);
            if(redisData2.getExpireTime().isAfter(LocalDateTime.now())){
                //说明已经被修改过了，直接返回
                return BeanUtil.copyProperties(redisData2.getData(), type);
            }

            BUILD_CACHE.submit(()->{
                try {
                    R r1 = dbFind.apply(id);
                    if(r1==null){
                        stringRedisTemplate.opsForValue().set(key,"", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                        return ;
                    }
                    RedisData redisData1=new RedisData();
                    redisData1.setData(r1);
                    redisData1.setExpireTime(LocalDateTime.now().plusMinutes(ttl));
                    stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData1));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    //注意，谁异步，谁负责放锁
                    unLock(lockKey);
                }


            });

        }
        //不管三七二十一，返回旧数据，之后过来的线程可以直接返回新的
        return r;
    }





    private boolean tryLock(String key ){
        Boolean isTrue=stringRedisTemplate.opsForValue().setIfAbsent(key,"1", RedisConstants.LOCK_SHOP_TTL,TimeUnit.MINUTES);
        return BooleanUtil.isTrue(isTrue);

    }
    private void unLock(String key ){
        stringRedisTemplate.delete(key);
    }
}
