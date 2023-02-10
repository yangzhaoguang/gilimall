package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/2 16:55
 * Description: 
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /*
     * 根据 skuId 获取 spu 信息
     * */
    @GetMapping("product/spuinfo/{skuId}")
    public R getSpuInfoBySkuId(@PathVariable("skuId")Long skuId);
}
