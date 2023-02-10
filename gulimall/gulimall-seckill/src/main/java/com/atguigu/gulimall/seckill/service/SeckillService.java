package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/7 11:15
 * Description: 
 */
public interface SeckillService {

    /**
     * @description
     * @date 2023/2/7 11:30
     * @param
     * @return void 上架最近三天的秒杀商品
     */
    void UploadSeckillSkuLatest3Days();

    /**
     * @description
     * @date 2023/2/7 20:15
     * @param
     * @return java.util.List<com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo>
     *     获取当前时间的秒杀商品
     */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * @description
     * @date 2023/2/8 10:44
     * @param skuId
     * @return com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo
     * 根据 skuId 查询某一个秒杀商品
     */
    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    /**
     * @description
     * @date 2023/2/8 12:46
     * @param killId 2_11 秒杀商品信息的key
     * @param key 随机码
     * @param num 秒杀数量
     * @return java.lang.String 秒杀功能
     */
    String kill(String killId, String key, Integer num);
}
