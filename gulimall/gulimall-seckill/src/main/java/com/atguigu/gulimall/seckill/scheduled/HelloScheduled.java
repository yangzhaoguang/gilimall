package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * Author: YZG
 * Date: 2023/2/7 9:27
 * Description:
 */
// @EnableScheduling
@Component
@Slf4j
// @EnableAsync
public class HelloScheduled {

    /*
    * 1、只允许6位：秒分时日周月
    * 2、周的位置上 1-7、MON-SUN，代表周一到周日
    * 3、定时任务不应该阻塞。
    *   （1）如果需要阻塞，使用 CompletableFuture 异步的方式，提交到线程池中
    *   （2）定时任务线程池. 配置 spring.task.scheduling.pool.size=5 ，有的版本不管用
    *   （4）使用异步任务注解
    *           @EnableAsync 开启异步任务
    *           @Async 希望异步执行的方法上增加该注解
    *           配置项都在 TaskExecutionProperties 中，可配置 corePool，MaxPool....
    *           spring.task.execution....
    * */
    // @Scheduled(cron = "*/1 * * * * *")
    @Async
    public void printHello() {
        try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
        log.info("hello");
    }
}
