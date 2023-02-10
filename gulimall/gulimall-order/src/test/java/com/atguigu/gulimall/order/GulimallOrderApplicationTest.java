package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 *
 * Author: YZG
 * Date: 2023/1/31 16:04
 * Description: 
 */
@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class GulimallOrderApplicationTest {

    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /*
    * 发送消息
    * */
    @Test
    public void senMessage() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(2L);
        orderEntity.setCreateTime(new Date());

        // String msg = "hello,world";
        // public void convertAndSend(String exchange, String routingKey, final Object object)
        // 交换机名称、路由键、消息
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderEntity);
        log.info("发送消息成功:{}",orderEntity);
    }

    /*
    * 创建一个交换机
    *   public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
    *   name 交换机名字
    *   durable 是否持久化
    *   autoDelete 是否自动删除
    *   arguments其他的一些参数
    * */
    @Test
    public void createExchange() {
        Exchange exchange = new DirectExchange("hello-java-exchange",true,false);
        // 声明一个交换机
        amqpAdmin.declareExchange(exchange);
        log.info("交换机创建成功:{}","hello-java-exchange");
    }

    /*
    * 创建一个队列
    *   public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
    *   name：队列名
    *   durable：是否持久化
    *   exclusive：是否是排他的
    *   autoDelete：是否自动删除
    *   arguments：其他的一些参数
    * */
    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("队列创建成功:{}","hello-java-queue");

    }

    /*
    * 创建绑定关系
    *   	public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
    *       Map<String, Object> arguments)
    *
    *       destination：绑定目标，绑定的队列名
    *       destinationType：绑定类型，QUEEN or EXCHANGE
    *       exchange： 绑定的交换机名
    *       routingKey：路由键
    *       arguments 其他参数
    * */
    @Test
    public void createBinding() {
        Binding binding = new Binding(
                "hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("绑定成功{}，{}","hello-java-exchange","hello-java-queue");
    }
}
