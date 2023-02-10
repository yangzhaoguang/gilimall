package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitResponseVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.jws.WebParam;
import java.util.concurrent.ExecutionException;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 14:49
 * Description: 
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;


    /*
    * 提交订单
    * */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes) {
        try {
            OrderSubmitResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode() == 0) {
                // 创建订单成功，跳转到支付页面
                model.addAttribute("submitOrderResp",responseVo);
                return  "pay";
            }else {
                // 创建失败、回到订单页面
                String msg = "下单失败";
                switch (responseVo.getCode()) {
                    case 1 : msg = "订单信息过期，请重新提交..." ; break;
                    case 2 : msg = "订单商品发生变化，请确认后再次提交"; break;
                    case 3 : msg = "商品库存不足";break;
                }
                attributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = ((NoStockException)e).getMessage();
                attributes.addFlashAttribute("msg",message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

    /*
    * 获取订单确认页数据
    * */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = orderService.orderConfirm();
        // 保存订单确认vo
        model.addAttribute("confirmOrderData",vo);
        return "confirm";
    }
}
