package com.atguigu.gulimall.order;



import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * Author: YZG
 * Date: 2022/12/25 17:24
 * Description: 
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableRabbit
@MapperScan("com.atguigu.gulimall.order.dao")
@EnableRedisHttpSession
@EnableFeignClients
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(exposeProxy = true) // 开启Aspect动态代理
public class GulimallOrderApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GulimallOrderApplication.class);
        // run.getBeanDefinitionNames();
        // Object messageConverter = run.getBean("messageConverter");
        // System.out.println(messageConverter);
    }
}
