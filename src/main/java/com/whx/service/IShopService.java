package com.whx.service;

import com.whx.dto.Result;
import com.whx.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IShopService extends IService<Shop> {

    Result queryShopById(Long id);

    Result updateShop(Shop shop);


    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
