package com.atguigu.gulimall.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 *
 * Author: YZG
 * Date: 2023/2/7 11:13
 * Description: 
 */
@EnableScheduling
@EnableAsync
@Configuration
public class ScheduledConfig {
}
