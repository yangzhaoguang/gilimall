package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SeckillFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/8 10:55
 * Description: 
 */
@FeignClient(value = "gulimall-seckill",fallback = SeckillFeignFallback.class)
public interface SeckillFeignService {

    /*
    * 查询某一个商品的秒杀信息
    * */
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable Long skuId);

}
