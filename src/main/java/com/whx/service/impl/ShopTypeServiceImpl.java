package com.whx.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.whx.dto.Result;
import com.whx.entity.ShopType;
import com.whx.mapper.ShopTypeMapper;
import com.whx.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whx.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@RequiredArgsConstructor
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        String key= RedisConstants.CACHE_SHOP_KEY+"shop-type";
        String s = stringRedisTemplate.opsForValue().get(key);
        List<ShopType>list=new ArrayList<>();
        if(!StrUtil.isBlankIfStr(s)){
            list = JSONUtil.toList(s, ShopType.class);
            return Result.ok(list);
        }
        //redis里面没有
        list=lambdaQuery().list();
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(list));
        return Result.ok(list);



    }
}
