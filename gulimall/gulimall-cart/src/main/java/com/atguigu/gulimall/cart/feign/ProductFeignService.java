package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/1/30 20:42
 * Description: 
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /*
    * 获取商品价格
    * */
    @RequestMapping("product/skuinfo/price/{skuId}")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId);
    /*
    * 查询sku基本信息
    * */
    @RequestMapping("product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);

    /*
    * 查询SKU的销售属性名、值。并封装成 List<String>
    * 格式：
    * [
    *   {可选版本:8+128G},
    *   {颜色:冰霜银}
    * ]
    * */
    @GetMapping("product/skusaleattrvalue/getSkuAttrList")
    public List<String> getSkuAttrValueAsStringList(@RequestParam("skuId") Long skuId);



}
