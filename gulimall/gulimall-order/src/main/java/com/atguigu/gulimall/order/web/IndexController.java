package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 10:14
 * Description: 
 */
@Controller
public class IndexController {

    @GetMapping("/{page}.html")
    public String toPage(@PathVariable String page) {
        return  page;
    }
}
