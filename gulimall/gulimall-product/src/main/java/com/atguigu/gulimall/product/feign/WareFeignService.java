package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/1/12 21:19
 * Description: 
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /*
    * 查询 sku 是否有库存
    * */
    @PostMapping("ware/waresku/hasStock")
    public Map<Long, Boolean> hasStock(@RequestBody List<Long> skuIds);
}
