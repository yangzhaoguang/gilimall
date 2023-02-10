package com.atguigu.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * Author: YZG
 * Date: 2023/1/25 20:00
 * Description:  线程池参数
 */
@ConfigurationProperties(prefix ="gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {

    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Long keepAliveTime;

}
