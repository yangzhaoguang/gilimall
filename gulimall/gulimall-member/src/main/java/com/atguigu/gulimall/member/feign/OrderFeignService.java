package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/5 21:59
 * Description: 
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    /*
    * 查询登录用户的所有订单
    * */
    @PostMapping("order/order/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params);
}
