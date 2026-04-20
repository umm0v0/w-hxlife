package com.w.controller;


import com.w.dto.Result;
import com.w.service.IVoucherOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Tag(name = "优惠券订单接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    private final IVoucherOrderService voucherOrderService;
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucherByLua(voucherId);
    }
}
