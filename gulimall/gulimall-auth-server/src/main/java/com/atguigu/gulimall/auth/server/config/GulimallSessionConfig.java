package com.atguigu.gulimall.auth.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

/**
 *
 * Author: YZG
 * Date: 2023/1/29 17:43
 * Description: 
 */
@Configuration
public class GulimallSessionConfig {

    /**
     * session存入redis序列化
     * */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /**
     *设置cookie域名范围
     * */
    @Bean
    public DefaultCookieSerializer webSessionIdResolver() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("GULISESSION");
        cookieSerializer.setDomainName("gulimall.com");
        return cookieSerializer;
    }
}
