package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 *
 * Author: YZG
 * Date: 2023/1/20 16:52
 * Description: 
 */
@Configuration
public class RedissonConfig {

    /*
    * 创建Redisson对象都通过RedissonClient
    * */
    @Bean(destroyMethod="shutdown")
   public  RedissonClient redisson() throws IOException {
        Config config = new Config();
        // 可使用 rediss 启用 SSH 安全连接
        config.useSingleServer().setAddress("redis://192.168.56.111:6379");
        // 创建对象
        return Redisson.create(config);
    }
}
