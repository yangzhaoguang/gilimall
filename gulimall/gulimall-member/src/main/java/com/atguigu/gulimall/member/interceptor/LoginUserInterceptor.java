package com.atguigu.gulimall.member.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 14:52
 * Description: 
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 解决远程调用  需要登录问题
        String requestURI = request.getRequestURI();
        // 匹配映射
        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean match = pathMatcher.match("/member/**", requestURI);
        if (match) {
            return true;
        }


        MemberRespVo memberRespVo = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberRespVo == null) {
            request.getSession().setAttribute("msg","请先登录");
            // 没有进行登录，跳转到登录界面登录
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }else{
            threadLocal.set(memberRespVo);
            return true;
        }
    }
}
