package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * Author: YZG
 * Date: 2023/1/5 18:05
 * Description: 远程调用 gulimall-coupon 模块
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {



    /**
     * SpringCloud远程调用流程:
     * 1、 调用 CouponFeignService.saveSpuBounds(spuBoundsTo) 方法
     *      (1)  @RequestBody 将 spuBoundsTo 这个对象转换 json
     *      (2) 在 Nacos服务中心找到gulimall-coupon服务，并向 coupon/spubounds/save 发送请求，
     *          并把 json 保存在请求体中
     *      (3) 对方接受到请求，(@RequestBody SpuBoundsEntity spuBounds)
     *          @RequestBody 会将 json 数据转换为 SpuBoundsEntity
     * 只要 json 中的字段名与 SpuBoundsEntity中的字段名保持一致，可以自动封装的。
     * */

    /*
    *  保存商品积分信息
    * */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    /*
    * 保存商品优惠信息
    * */
    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
