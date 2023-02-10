package com.atguigu.gulimall.auth.server.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 11:43
 * Description: 
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    /**
     * 发送验证码
     * */
    @RequestMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code);
}
