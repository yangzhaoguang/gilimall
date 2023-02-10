package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 *
 * Author: YZG
 * Date: 2023/2/5 11:27
 * Description: 消费者：自动解锁库存
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /*
    *
    * */
    @RabbitHandler
    public void handleStockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        try {
            System.out.println("收到库存解锁通知...");
            wareSkuService.unLock(to);
            // 手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 有任何异常，都是解锁失败
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            e.printStackTrace();
        }
    }

    /*
    * 收到订单关闭通知，解锁库存
    * */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo order, Message message, Channel channel) throws IOException {
        try {
            System.out.println("订单关闭，即将解锁库存....");
            wareSkuService.unLock(order);
            // 手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 有任何异常，都是解锁失败
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            e.printStackTrace();
        }
    }
}
