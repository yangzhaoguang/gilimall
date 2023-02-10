package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 * Author: YZG
 * Date: 2023/2/4 18:26
 * Description：
 */
@Configuration
public class MyMQConfig {



    /*
    * 使用 @Bean 的方式创建 Exchange、Queue、Binding...服务启动会自动向RabbitMQ创建。
    * 前提是RabbitMQ中没有这些  Exchange、Queue、Binding... 如果存在，即使配置不一样也不会重新创建。
    * */


    // 延迟队列
    @Bean
    public Queue orderDelayQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        HashMap<String, Object> arguments = new HashMap<>();
        // 设置与队列相连的死信交换机
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        // 转发死信的 路由键
        arguments.put("x-dead-letter-routing-key","order.release.order");
        // 设置队列的 TTL。超过1min就表示未支付订单，准备关闭
        arguments.put("x-message-ttl",60000);

        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    // 普通队列
    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue",true,false,false,null);
    }

    // 交换机
    @Bean
    public TopicExchange orderEventExchange() {
        //String name, boolean durable, boolean autoDelete)
        return new TopicExchange("order-event-exchange",true,false);
    }

    // 设置绑定关系: order-event- exchange ——》order.delay.queue
    @Bean
    public Binding orderCreateOrder() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,Map<String, Object> arguments
        // 绑定目的地-绑定的队列，绑定类型【交换机 OR 队列】，交换机，路由键，其他参数信息
        return new Binding(
                "order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    // 设置绑定关系: order-event- exchange ——》order.release.order.queue
    @Bean
    public Binding orderReleaseOrder() {
        return new Binding(
                "order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    // 设置绑定关系: order-event- exchange ——》stock.release.stock.queue
    @Bean
    public Binding orderReleaseOther() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    /**
     * 商品秒杀队列
     * @return
     */
    @Bean
    public Queue orderSecKillOrrderQueue() {
        Queue queue = new Queue("order.seckill.order.queue", true, false, false);
        return queue;
    }

    @Bean
    public Binding orderSecKillOrrderQueueBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments
        Binding binding = new Binding(
                "order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);

        return binding;
    }
}
