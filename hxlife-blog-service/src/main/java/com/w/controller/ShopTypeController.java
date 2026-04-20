package com.w.controller;


import com.w.dto.Result;
import com.w.service.IShopTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Tag(name = "商铺类型接口")
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @Operation(summary = "查询商铺类型列表")
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
