package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.Result;
import com.w.entity.Voucher;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
