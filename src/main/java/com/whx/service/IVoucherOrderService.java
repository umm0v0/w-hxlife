package com.whx.service;

import com.whx.dto.Result;
import com.whx.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucherByLua(Long voucherId);

    //Result seckillVoucher(Long voucherId);

    //Result createVoucher(Long voucherId);

    @Transactional
    Result createVoucher02(VoucherOrder voucherOrder);

    void cancleOrder(Long id);
}
