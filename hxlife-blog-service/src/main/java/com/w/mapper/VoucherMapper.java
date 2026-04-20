package com.w.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.w.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
