package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 14:55
 * Description: 
 */
@Configuration
public class OrderWebConfig implements WebMvcConfigurer {
    /*
    * 增加拦截器
    * */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginUserInterceptor()).addPathPatterns("/**").excludePathPatterns("/order/order/status/**").excludePathPatterns("/payed/notify");
    }
}
