package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 *
 * Author: YZG
 * Date: 2023/1/31 17:15
 * Description: 
 */
@Configuration
public class MyRabbitConfig {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /*
     * 这里直接注入MessageConverter会有循环依赖的问题
     *   构建 RabbitTemplate 时需要 MessageConverter，
     *   而MessageConverter由依赖于 MyRabbitConfig，MyRabbitConfig 中又注入了 RabbitTemplate ，造成了循环依赖。
     * */
    // @Bean
    // public MessageConverter messageConverter() {
    //     return new Jackson2JsonMessageConverter();
    // }

    /*
     * 设置发布确认机制
     *   1、ConfirmCallback，只要 Broker 接收到消息就会执行此回调
     *      spring.rabbitmq.publisher-confirms=true
     *   2、ReturnCallback 只有交换机将消息转发到Queue失败时，才会调用此回调
     *      # 开启发送端确认机制。 Exchange --> Queue
     *      spring.rabbitmq.publisher-returns=true
     *      # 只要消息成功发送到Queue，就优先异步调用 ReturnCallback
     *      spring.rabbitmq.template.mandatory=true
     * */
    @PostConstruct // MyRabbitConfig初始化之后执行
    public void InitRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @description
             * @date 2023/1/31 18:55
             * @param correlationData 保存消息的id以及相关信息，可在发送消息时指定 new CorrelationData()
             * @param ack 消息是否发送失败。true：Broke接收到消息， false：Broker没有接收到消息
             * @param cause 消息发送失败的原因
             * @return void
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack) {
                    System.out.println("Broker接收消息成功, correlationData: " + correlationData + " ack:" + ack + " cause:" + cause);
                } else {
                    System.out.println("Broker接收消息失败, correlationData: " + correlationData + " ack:" + ack + " cause:" + cause);
                }
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * @description
             * @date 2023/1/31 22:25
             * @param message 投递失败的消息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本
             * @param exchange  投递失败的交换机
             * @param routingKey    投递失败消息的 routing-key
             * @return void
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("message: " + message + " replyCode: " + replyCode + " replyText: " + replyText + " exchange: " + exchange + " routingKey: " + routingKey);
            }
        });
    }
}

@Configuration
class messageConverterConfig{
    /*
     * 自定义消息转换器
     * */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
