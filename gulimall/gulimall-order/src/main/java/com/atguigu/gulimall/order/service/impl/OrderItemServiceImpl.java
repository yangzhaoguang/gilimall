package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;


@Service
@RabbitListener(queues = {"hello-java-queue"})
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /*
    * RabbitMQ 接收消息
    *   1、使用 @RabbitListener 接收消息，必须使用 @EnableRabbit 开启接受消息
    *       queueu 是一个 String[], 可指定接受多个队列的消息
    *   参数可接收的类型:
    *       rg.springframework.amqp.core.Message;: 封装的内容比较全，消息头+消息体
    *       T : 可接收发送消息的类型
    *       Channel channel : 信道信息
    *       (1) 在多服务下，一条消息只能有一个客户端接收
    *       (2) 处理完一条消息后，才能接收下一条消息
    *
    *   2、使用 @RabbitHandler +  @RabbitListener 接受不同类型的消息
    *        @RabbitHandler：标注在方法上
    *        @RabbitListener： 标注在类、方法上
    *
    *   3、消费端手动消息确认
    *       channel.basicAck
    *   4、拒绝消息
    *       channel.basicReject();
    *       channel.basicNack()
    * */
    // @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveOrderEntityMessage(Message message, OrderEntity entity, Channel channel) {

        // byte[] body = message.getBody();
        // MessageProperties header = message.getMessageProperties();
        // System.out.println("接收到的消息: " + message);
        // System.out.println("接收到的消息体：" + entity);

        /*
        *  消息确认
        *       void basicAck(long deliveryTag, boolean multiple) throws IOException;
        *           deliveryTag: 消息标签，channel内顺序自增
        *           multiple 是否批量确认
        *   拒绝消息
        *       void basicNack(long deliveryTag, boolean multiple, boolean requeue)
        *            multiple 是否批量拒绝消息
        *            requeue 拒绝的消息是否重新入队。如果重新入队还重新发送给消费者
        *       void basicReject(long deliveryTag, boolean requeue) throws IOException;
        *           与 basicNack 区别就是没有批量拒绝消息
        *
        * */
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (deliveryTag % 2 == 0) {
                // 手动确认消息
                channel.basicAck(deliveryTag,false);
                System.out.println("签收了货物..." + deliveryTag);
            }else {
                // 拒绝消息
                channel.basicNack(deliveryTag,false,true);
                // channel.basicReject();
                System.out.println("没有签收货物..." + deliveryTag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // @RabbitHandler
    public void receiveOrderReturnApplyEntityMessage(Message message, OrderReturnApplyEntity entity,Channel channel) {
        // System.out.println("接收到的消息: " + message);
        System.out.println("接收到的消息体：" + entity);
    }
}