package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/2/7 11:32
 * Description: 
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /*
     * 查询最近三天的秒杀场次
     * */
    @GetMapping("coupon/seckillsession/findLatest3DaysSessions")
    public R findLatest3DaysSessions();
}
