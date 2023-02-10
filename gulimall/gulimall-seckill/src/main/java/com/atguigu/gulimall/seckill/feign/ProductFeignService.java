package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/7 16:29
 * Description: 
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /*
    * 查询商品的sku信息
    * */
    @RequestMapping("product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
