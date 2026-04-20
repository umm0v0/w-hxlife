package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.Result;
import com.w.entity.Shop;

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
