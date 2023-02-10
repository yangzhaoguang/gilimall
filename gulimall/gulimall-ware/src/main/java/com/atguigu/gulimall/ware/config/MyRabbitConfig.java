package com.atguigu.gulimall.ware.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 *
 * Author: YZG
 * Date: 2023/2/4 21:26
 * Description: 
 */
@Configuration
public class MyRabbitConfig {
    /*
     * 自定义消息转换器
     * */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    /*
    * 创建交换机
    * */
    @Bean
    public Exchange stockEventExchange() {
        return  new TopicExchange("stock-event-exchange",true,false,null);
    }

    /*
    * 创建普通队列
    * */
    @Bean
    public Queue stockReleaseStockQueue() {
        return  new Queue("stock.release.stock.queue",true,false,false,null);
    }

    /*
    * 创建延迟队列
    * */
    @Bean
    public Queue stockDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        // 设置与队列相连的死信交换机
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        // 转发死信的 路由键
        arguments.put("x-dead-letter-routing-key","stock.release");

        arguments.put("x-message-ttl",120000);
        return  new Queue("stock.delay.queue",true,false,false,arguments);
    }

    /*
    * 交换机与延迟队列绑定
    * */
    @Bean
    public Binding stockFinish() {
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

    /*
    * 交换机与普通队列绑定
    * */
    @Bean
    public Binding stockRelease() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }
}
