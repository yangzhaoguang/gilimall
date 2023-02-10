package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/2 9:13
 * Description: 
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /*
     * 查询sku是否有库存
     * */
    @PostMapping("ware/waresku/hasStock")
    public HashMap<Long, Boolean> hasStock(@RequestBody List<Long> skuIds);

    /*
     * 计算运费
     * */
    @GetMapping("ware/wareinfo/fare")
    public R fare(@RequestParam("addrId")Long  addrId);


    /*
    * 锁定库存
    * */
    @PostMapping("ware/wareinfo/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);
}
