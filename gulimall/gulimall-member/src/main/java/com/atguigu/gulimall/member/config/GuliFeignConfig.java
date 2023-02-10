package com.atguigu.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 17:28
 * Description:  Feign 远程调用拦截器
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){

        return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 获取老请求。和 Controller 中传入的 HttpServletRequest 一样
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    requestTemplate.header("Cookie",request.getHeader("Cookie"));
                }
            }
        };
    }
}
