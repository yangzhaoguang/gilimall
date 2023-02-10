package com.xunqi.gulimall.ssoclient.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {

    /**
     * 无需登录就可访问
     *
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/hello")
    public String hello() {
        return "hello";
    }


    @GetMapping(value = "/boss")
    public String employees(Model model,
                            HttpSession session,
                            @RequestParam(value = "token",required = false)String token) {
        if (!StringUtils.isEmpty(token)) {
            // 登录过。
            // 是拿着token去 sso-server查询用户信息
            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<String> response = restTemplate.getForEntity("http://ssoserver.com:8080/userinfo?token=" + token, String.class);
            // 查询出来的数据存到 session 中
            session.setAttribute("loginUser",response.getBody());
        }
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            // 没有登录，跳转到 中心
            return "redirect:http://ssoserver.com:8080/login.html?redirect_url=http://client2.com:8082/boss";
        } else {
            List<String> boss = new ArrayList<>();

            boss.add("李老板");
            boss.add("张老板");

            model.addAttribute("boss", boss);
            return "boss";
        }
    }

}
