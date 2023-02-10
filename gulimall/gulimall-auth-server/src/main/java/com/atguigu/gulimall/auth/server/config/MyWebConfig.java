package com.atguigu.gulimall.auth.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 9:48
 * Description: 
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    /*
    * ViewController——视图与请求的映射关系
    * */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        /**
         *     @RequestMapping("login.html")
         *     public String login() {
         *         return  "login";
         *     }
         * */
        // registry.addViewController("login.html").setViewName("login");
        /**
         *     @RequestMapping("reg.html")
         *     public String reg() {
         *         return  "reg";
         *     }
         * */
        registry.addViewController("reg.html").setViewName("reg");
    }
}
