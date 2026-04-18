package com.whx.service;

import com.whx.dto.Result;
import com.whx.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryTypeList();
}
