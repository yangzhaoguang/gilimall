package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import com.fasterxml.classmate.MemberResolver;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 *
 * Author: YZG
 * Date: 2023/1/30 17:16
 * Description: 
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /*
    * 执行目标方法之前执行
    *   判断用户的登录状态
    *       登录：封装登录的用户信息
    *       没有登录：判断cookie中是否有user-key，如果有直接封装 user-key
    *   无论是登录状态，还是临时用户都应该创建出一个 user-key
    * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 先从 redis 中获取用户信息(整合了SpringSession后，会将session包装，从redis中取数据)
        HttpSession session = request.getSession();
        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        UserInfoTo userInfoTo = new UserInfoTo();

        if (memberRespVo != null) {
            // 登录状态
            userInfoTo.setUserId(memberRespVo.getId());
        }
            // 没有登录
            Cookie[] cookies = request.getCookies();
            // 判断cookie中是否有user-key
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (CartConstant.TEMP_USER_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                        // 如果有直接封装
                        userInfoTo.setUserKey(cookie.getValue());
                        userInfoTo.setTempUser(true);
                    }
                }
            }
            // 无论是登录状态，还是临时用户都设置一个 userkey
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);

        }

        // 保存到本地变量池
        threadLocal.set(userInfoTo);
        return  true;
    }

    /*
    * 目标方法执行之后执行
    *   通知浏览器保存带有user-key的cookie
    * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        // 通知浏览器保存带有user-key的cookie
        // 只设置一次
        if (!userInfoTo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(60*60*24*30);
            response.addCookie(cookie);
        }
    }
}
