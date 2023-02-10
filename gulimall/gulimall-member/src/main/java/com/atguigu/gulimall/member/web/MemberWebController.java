package com.atguigu.gulimall.member.web;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import javafx.beans.binding.ObjectExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Author: YZG
 * Date: 2023/2/5 20:53
 * Description: 
 */
@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    /*
    * 查询登录用户的所有订单
    * */
    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, Model model) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("page",pageNum.toString());
        R r = orderFeignService.listWithItem(params);
        model.addAttribute("orders",r);
        return "orderList";
    }
}
