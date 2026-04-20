package com.w.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.entity.SeckillVoucher;
import com.w.mapper.SeckillVoucherMapper;
import com.w.service.ISeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
