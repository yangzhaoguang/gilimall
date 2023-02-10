package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsSendComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 11:30
 * Description: 
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsSendComponent sendComponent;

    @RequestMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code){
        try {
            sendComponent.sendCode(phone,code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }
}
