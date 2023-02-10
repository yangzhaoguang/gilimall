package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 *
 * Author: YZG
 * Date: 2023/1/31 18:01
 * Description: 
 */
@RestController
@Slf4j
public class RabbitController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 模拟生成订单，向MQ发送消息
    @RequestMapping("/sendMQ/createOrder")
    public String createOrder() {
        // 创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());

        // 向MQ发送消息，监听订单是否支付成功
        // String exchange, String routingKey, Object message,CorrelationData correlationData
        // 交换机、消息的路由键，发送的消息，消息的唯一标识
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);

        return "Order created !!";
    }



    @RequestMapping("/sendMQ/{num}")
    public String sendMQ(@PathVariable("num") Integer num) {
        for (Integer i = 0; i < num; i++) {
            if (i % 2 == 0) {
                OrderEntity orderEntity = new OrderEntity();

                orderEntity.setId(i.longValue());
                orderEntity.setCreateTime(new Date());
                /*
                * 	public void convertAndSend(String exchange, String routingKey, final Object object,@Nullable CorrelationData correlationData)
                *   exchange: 交换机名称
                *   routingKey 路由键
                *   object 消息体
                *   CorrelationData  消息 id
                * */
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderEntity,new CorrelationData(UUID.randomUUID().toString()));
                // log.info("发送消息成功:{}",i);
            }else {
                OrderReturnApplyEntity orderReturnApplyEntity = new OrderReturnApplyEntity();
                orderReturnApplyEntity.setId(i.longValue());
                orderReturnApplyEntity.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnApplyEntity,new CorrelationData(UUID.randomUUID().toString()));
                // log.info("发送消息成功:{}",i);

            }
        }

        return  "sendMQ OK";
    }
}
