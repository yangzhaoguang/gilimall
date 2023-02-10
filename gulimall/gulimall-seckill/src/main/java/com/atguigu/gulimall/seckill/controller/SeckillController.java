package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/2/7 20:14
 * Description: 
 */
@Controller
public class SeckillController {

    @Autowired
    private SeckillService seckillService;


    /*
     * 秒杀
     * http://seckill.gulimall.com/kill?killId=2-11&key=f7bde6d931e34b7f8677b3395157d426&num=1
     * */
    @GetMapping("/kill")
    public String kill(
            @RequestParam("killId") String killId,
            @RequestParam("key") String key,
            @RequestParam("num") Integer num,
            Model model) {

        String orderSn = seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }

    /*
     * 获取当前时间的秒杀商品
     * */
    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> list = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(list);
    }

    /*
     * 查询某一个秒杀商品
     * */
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable Long skuId) {
        SeckillSkuRedisTo seckillSkuRedisTo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(seckillSkuRedisTo);
    }
}
