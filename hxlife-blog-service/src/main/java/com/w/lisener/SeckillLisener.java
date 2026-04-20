package com.w.lisener;


import com.w.entity.VoucherOrder;
import com.w.service.IVoucherOrderService;
import com.w.utils.RabbitMqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SeckillLisener {
    private final IVoucherOrderService voucherOrderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMqConstants.SECKILL_QUEUE),
            exchange = @Exchange(name = RabbitMqConstants.SECKILL_EXCHANGE),
            key = RabbitMqConstants.SECKILL_ORDER_KEY
    ))
    public void ListenSeckillOrder(VoucherOrder voucherOrder){
        log.info("从消息队列接收到订单");

        try {
            voucherOrderService.createVoucher02(voucherOrder);
        } catch (Exception e) {
            log.error("订单处理异常");
        }
    }

}
