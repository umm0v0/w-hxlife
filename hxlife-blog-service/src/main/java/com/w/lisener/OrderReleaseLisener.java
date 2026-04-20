package com.w.lisener;


import com.w.service.IVoucherOrderService;
import com.w.utils.RabbitMqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderReleaseLisener {

    private final IVoucherOrderService voucherOrderService;
    @RabbitListener(queues = RabbitMqConstants.SECKILL_DEAD_QUEUE)
    public void onOrderRelease(Long id){
        log.info("订单释放，订单id：{}",id);
        //更新订单状态为已取消
        //更新订单状态
        voucherOrderService.cancleOrder(id);
    }
}
