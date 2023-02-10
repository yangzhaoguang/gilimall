package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 *
 * Author: YZG
 * Date: 2023/2/7 11:14
 * Description: 
 */
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;
    @Autowired
    private RedissonClient redissonClient;

    private  final String UPLOAD_LOCK = "skuKill:upload:lock";
    /**
     * @description
     * @date 2023/2/7 11:29
     * @param
     * @return void
     * 上架最近三天的秒杀商品
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void UploadSeckillSkuLatest3Days() {
        // TODO: 保证幂等性，重复上架商品。 解决：分布式锁、并保证每个存储操作都是幂等性的
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);

        lock.lock(10, TimeUnit.SECONDS);
        try {
            log.info("准备上架商品...");
            seckillService.UploadSeckillSkuLatest3Days();
            log.info("上架成功...");
        } finally {
            lock.unlock();
        }
    }

}
