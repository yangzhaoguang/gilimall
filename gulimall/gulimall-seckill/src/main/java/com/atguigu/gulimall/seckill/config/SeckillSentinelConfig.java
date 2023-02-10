package com.atguigu.gulimall.seckill.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * Author: YZG
 * Date: 2023/2/8 19:42
 * Description:
 */
@Configuration
public class SeckillSentinelConfig {

    /*
     * 自定义sentinel流控失败信息, 俩种配置方式
     * */
    public SeckillSentinelConfig() {
        WebCallbackManager.setUrlBlockHandler(new UrlBlockHandler() {
            @Override
            public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
                // 响应编码
                httpServletResponse.setCharacterEncoding("UTF-8");
                // 数据格式
                httpServletResponse.setContentType("application/json");
                R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
                // 响应到页面
                httpServletResponse.getWriter().write(JSON.toJSONString(error));

            }
        });

    }

    // @Bean
    // public UrlBlockHandler urlBlockHandler(){
    //     UrlBlockHandler urlBlockHandler = new UrlBlockHandler() {
    //         @Override
    //         public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
    //             // 响应编码
    //             httpServletResponse.setCharacterEncoding("UTF-8");
    //             // 数据格式
    //             httpServletResponse.setContentType("application/json");
    //             R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    //             // 响应到页面
    //             httpServletResponse.getWriter().write(JSON.toJSONString(error));
    //         }
    //     };
    //     WebCallbackManager.setUrlBlockHandler(urlBlockHandler);
    //     return urlBlockHandler;
    // }
}
