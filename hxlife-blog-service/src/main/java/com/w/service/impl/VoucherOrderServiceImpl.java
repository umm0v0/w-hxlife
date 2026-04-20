package com.w.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.dto.Result;
import com.w.entity.SeckillVoucher;
import com.w.entity.VoucherOrder;
import com.w.mapper.VoucherOrderMapper;
import com.w.service.ISeckillVoucherService;
import com.w.service.IVoucherOrderService;
import com.w.utils.RabbitMqConstants;
import com.w.utils.RedisConstants;
import com.w.utils.RedisIdWorker;
import com.w.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    private final Redisson redisson;
    private final RedisIdWorker redisIdWorker;
    private final ISeckillVoucherService seckillVoucherService;
    private final RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long>SECKILL_SCRIPT;
    static{
        SECKILL_SCRIPT=new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);

    }

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public Result seckillVoucherByLua(Long voucherId){
        Long userId=UserHolder.getUser().getId();
        //执行lua脚本
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString());
        int ans=result.intValue();
        if(ans!=0){
            return Result.fail(ans==1?"库存不足":"禁止重复下单");
        }

        //创建订单，发送到消息队列
        VoucherOrder voucherOrder=new VoucherOrder();
        voucherOrder.setId(redisIdWorker.nextId("order"));
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setStatus(1);
        rabbitTemplate.convertAndSend(RabbitMqConstants.SECKILL_EXCHANGE,RabbitMqConstants.SECKILL_ORDER_KEY,voucherOrder);

        //发送延迟消息
        rabbitTemplate.convertAndSend(RabbitMqConstants.SECKILL_DELAY_EXCHANGE,RabbitMqConstants.SECKILL_DELAY_KEY,voucherOrder.getId());
        return Result.ok(voucherOrder.getId());
    }





    @Transactional
    @Override
    public Result createVoucher02(VoucherOrder voucherOrder){
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        //扣减库存
        boolean isSuccess = seckillVoucherService.lambdaUpdate().eq(SeckillVoucher::getVoucherId, voucherId)
                .setSql("stock=stock-1")
                .gt(SeckillVoucher::getStock, 0).update();
        if(!isSuccess){
            return Result.fail("扣减库存失败");
        }
        save(voucherOrder);
        return Result.ok(voucherOrder.getId());
    }

    private static final DefaultRedisScript<Long>ROLLBACK_SCRIPT;
    static{
        ROLLBACK_SCRIPT=new DefaultRedisScript<>();
        ROLLBACK_SCRIPT.setLocation(new ClassPathResource("rollback.lua"));
        ROLLBACK_SCRIPT.setResultType(Long.class);
    }

    @Override
    @Transactional
    public void cancleOrder(Long id) {
        VoucherOrder voucherOrder = lambdaQuery().eq(VoucherOrder::getId, id).one();
        if(voucherOrder==null){
            log.info("订单不存在");
            return;
        }
        if(voucherOrder.getStatus()!=1){
            log.info("订单已经处理过了");
            return;
        }
        //System.out.println(voucherOrder);
        boolean isSuccess = lambdaUpdate().eq(VoucherOrder::getId, id).eq(VoucherOrder::getStatus,1)
                .setSql("status=4")
                .update();
        if(!isSuccess){
            log.error("取消订单失败，订单id：{}", id);
            return;
        }
        //再修改redis
        //修改redis当中的库存
        //直接改为利用lua脚本来修改
        Long result = stringRedisTemplate.execute(
                ROLLBACK_SCRIPT,
                Collections.emptyList(),
                RedisConstants.SECKILL_STOCK_KEY + voucherOrder.getVoucherId(),
                RedisConstants.SECKILL_ORDER_KEY + voucherOrder.getVoucherId(),
                voucherOrder.getUserId().toString()
        );
        log.info("redis回滚结果，{},订单id{}",result,id);
        log.info("取消订单成功，订单id：{}",id);

    }



//    private VoucherOrder createVoucherOrder(Long voucherId) {
//        VoucherOrder voucherOrder = new VoucherOrder();
//        voucherOrder.setVoucherId(voucherId);
//        voucherOrder.setUserId(UserHolder.getUser().getId());
//        voucherOrder.setStatus(1);
//        voucherOrder.setCreateTime(LocalDateTime.now());
//        voucherOrder.setId(redisIdWorker.nextId("order"));
//        voucherOrder.setUpdateTime(LocalDateTime.now());
//        save(voucherOrder);
//        return voucherOrder;
//
//    }


//    @Override
//    @Transactional
//    public Result createVoucher(Long voucherId){
//        //查询优惠卷
//        SeckillVoucher seckillVoucher = seckillVoucherService.lambdaQuery().eq(SeckillVoucher::getVoucherId, voucherId).one();
//        if(seckillVoucher==null){
//            return Result.fail("优惠卷不存在");
//        }
//
//        //判断该用户是否下过单了
//        Long userId = UserHolder.getUser().getId();
//        Long count = lambdaQuery().eq(VoucherOrder::getUserId, userId).eq(VoucherOrder::getVoucherId,voucherId).count();
//        if(count!=null&&count>0){
//            return Result.fail("不允许下超过一张订单");
//        }
//
//        //判断库存
//        if(seckillVoucher.getStock()<=0){
//            return Result.fail("优惠卷库存不足");
//        }
//        //利用乐观锁来修改库存，防止超卖
//        boolean update = seckillVoucherService.lambdaUpdate().eq(SeckillVoucher::getVoucherId, voucherId)
//                .setSql("stock=stock-1")
//                .gt(SeckillVoucher::getStock, 0).update();
//        if(!update){
//            return Result.fail("下单失败，库存不足");
//        }
//        VoucherOrder voucherOrder=createVoucherOrder(voucherId);
//        return Result.ok(voucherOrder.getId());
//    }


//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        Long userId=UserHolder.getUser().getId();
//        RLock lock=redisson.getLock(RedisConstants.LOGIN_USER_KEY+userId);
//        boolean isLock = lock.tryLock();
//        if(!isLock){
//            return Result.fail("禁止重复下单");
//        }
//        Result result=null;
//        try{
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            result=proxy.createVoucher(voucherId);
//        }catch (Exception e){
//            return Result.fail("优惠卷购买失败");
//        }finally {
//            lock.unlock();
//        }
//
//        return result;
//    }
}
