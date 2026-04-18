package com.whx.config;



import com.whx.utils.RabbitMqConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MqConfig {
    //交换机

    @Bean
    public Exchange orderDelayExchange(){
        return ExchangeBuilder.directExchange(RabbitMqConstants.SECKILL_DELAY_EXCHANGE).build();
    }

    @Bean
    public Exchange orderDeadExchange(){
        return ExchangeBuilder.directExchange(RabbitMqConstants.SECKILL_DEAD_EXCHANGE).build();
    }

    //队列

    //延迟队列,绑定里面消息过期送给的死信交换机
    @Bean
    public Queue orderDelayQueue(){
        //TODO:延迟消息改为30秒方便测试
        return QueueBuilder.durable(RabbitMqConstants.SECKILL_DELAY_QUEUE)
                .ttl(30000)
                .deadLetterExchange(RabbitMqConstants.SECKILL_DEAD_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConstants.SECKILL_DEAD_KEY)
                .build();
    }

    @Bean
    public Queue orderDeadQueue(){
        return QueueBuilder.durable(RabbitMqConstants.SECKILL_DEAD_QUEUE).build();
    }


    //绑定队列和交换机,绑定延迟队列和死信交换机

    //绑定延迟交换机延时队列
    @Bean
    public Binding orderCreateBinding() {
        // 参数：目的队列名，目的地类型，交换机名，路由键，参数Map
        return new Binding(
                RabbitMqConstants.SECKILL_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMqConstants.SECKILL_DELAY_EXCHANGE,
                RabbitMqConstants.SECKILL_DELAY_KEY,
                null
        );
    }

    //绑定死信交换机和死信队列
    @Bean
    public Binding orderDeadBinding(){
        return new Binding(
                RabbitMqConstants.SECKILL_DEAD_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMqConstants.SECKILL_DEAD_EXCHANGE,
                RabbitMqConstants.SECKILL_DEAD_KEY,
                null
        );
    }
}
