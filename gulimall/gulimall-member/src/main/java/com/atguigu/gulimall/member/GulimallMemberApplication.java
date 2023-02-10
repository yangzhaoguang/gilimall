package com.atguigu.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

/**
 *
 * Author: YZG
 * Date: 2023/1/3 22:32
 * Description: 
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableSpringHttpSession
@EnableFeignClients
public class GulimallMemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class);
    }
}
