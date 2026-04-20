package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.Result;
import com.w.entity.VoucherOrder;
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
