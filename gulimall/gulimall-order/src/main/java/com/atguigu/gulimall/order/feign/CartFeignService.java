package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/1 15:59
 * Description: 
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    /*
     * 获取用户所有勾选的购物项
     * */
    @GetMapping("/currentUserCartItems")
    public List<OrderItemVo> currentUserCartItems();
}
