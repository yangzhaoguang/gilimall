package com.xunqi.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    /*
    * 获取用户信息
    * */
    @ResponseBody
    @GetMapping("/userinfo")
    public String userinfo(@RequestParam(value = "token") String token) {
        String s = redisTemplate.opsForValue().get(token);

        return s;
    }


    /*
    * 感知之前是否有人登陆过
    * */
    @GetMapping("/login.html")
    public String loginPage(@CookieValue(value = "sso_token",required = false) String token,
                            @RequestParam("redirect_url")String url,Model model) {
        // 感知其他系统是否有登录，查看是否有cookie
        // 这里不应该只判断token是否为空，还需要判断能否在redis中取出用户信息。
        // 否则别人只要随便创建一个 sso_token的cookie就能够免登录了
        String s = redisTemplate.opsForValue().get(token);
        if (!StringUtils.isEmpty(token) && !StringUtils.isEmpty(s) ) {
            return "redirect:" + url + "?token=" + token;
        }
        model.addAttribute("url",url);
        return "login";
    }

    /*
    * 实现登录
    * */
    @PostMapping(value = "/doLogin")
    public String doLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("redirect_url") String url,
            HttpServletResponse response
    ) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            // 将uuid作为token
            String token = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(token,username);
            // 如何让其他系统感知到登录操作？ 使用 cookie
            // 在本域名：sso-server 下创建一个cookie，在每次发送请求时，都会在请求头中携带cookie
            response.addCookie(new Cookie("sso_token",token));
            // 登录成功,将token返回给客户端，用于判断是否登录
            return "redirect:" + url + "?token=" + token;
        }

        return "login";
    }

}
