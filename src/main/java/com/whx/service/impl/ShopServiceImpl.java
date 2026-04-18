package com.whx.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whx.dto.Result;
import com.whx.entity.Shop;
import com.whx.mapper.ShopMapper;
import com.whx.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whx.utils.CacheClient;
import com.whx.utils.RedisConstants;
import com.whx.utils.SystemConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.PlatformManagedObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheClient cacheClient;
    @Override
    public Result queryShopById(Long id) {
        //首先去查一下redis里面有没有,同时，根据一些一段时间内爆火的店铺做一些处理
        Shop shop = cacheClient.getByIdWithMutex(RedisConstants.CACHE_SHOP_KEY, id, Shop.class,
                id2 -> lambdaQuery().eq(Shop::getId, id2).one(),RedisConstants.CACHE_SHOP_TTL,TimeUnit.MINUTES);

        return shop!=null?Result.ok(shop):Result.fail("店铺不存在");
    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        //保证双写一致
        //先改数据库
        Long id= shop.getId();
        if(id==null){
            return Result.fail("商铺不存在");
        }
        boolean isSuccess = updateById(shop);
        if(!isSuccess){
            return Result.fail("修改失败，请重试");
        }
        //再删缓存
        String key=RedisConstants.CACHE_SHOP_KEY+id;
        stringRedisTemplate.delete(key);
        log.info("缓存已删除：{}",key);
        return Result.ok();

    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        if(x==null||y==null){
            //走传统分页
            Page<Shop>page=lambdaQuery().eq(Shop::getTypeId,typeId).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        String key=RedisConstants.SHOP_GEO_KEY+typeId;
        int from=(current-1)*SystemConstants.MAX_PAGE_SIZE;
        int end=current*SystemConstants.MAX_PAGE_SIZE;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(key,
                GeoReference.fromCoordinate(x, y),
                new Distance(5000),//搜索5公里内的
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().
                        includeDistance().//包含距离信息
                        limit(end));//只能收到第end条，后面截断一下

        if(results==null){
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if(list.size()<from){
            //内容不足以支撑到下一页的查询
            return Result.ok(Collections.emptyList());
        }
        List<Long>ids=new ArrayList<>(list.size());
        Map<String,Distance>distanceMap=new HashMap<>(list.size());
        list.stream().forEach(result->{
            String shopIdStr=result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            distanceMap.put(shopIdStr,result.getDistance());
        });
        String join = StrUtil.join(",", ids);
        List<Shop> shops = lambdaQuery().in(Shop::getId, ids).last("order by field(id," + join + ")").list();
        for(Shop shop:shops){
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);

    }
}
