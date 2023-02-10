package com.atguigu.gulimall.auth.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.server.feign.MemberFeignService;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.server.vo.SocialUserVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 *
 * Author: YZG
 * Date: 2023/1/29 10:42
 * Description: 第三方登录
 */
@Controller
public class Auth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 处理回调URL
     * */
    @GetMapping("/oauth2.0/weibo/success")
    public String oauthLogin(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 封装参数
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "1203861779");
        map.put("client_secret", "0a5e60edc13e444fca26a95c3847b7f7");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        // 发送请求
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());

        if (response.getStatusLine().getStatusCode() == 200) {
            // 请求成功
            // 获取响应数据 => JSON => 对应的实体类
            String json = EntityUtils.toString(response.getEntity());
            SocialUserVo socialUser = JSON.parseObject(json, SocialUserVo.class);
            // 判断用户是否是第一次登录,如果是第一次登录就注册并返回用户信息，如果不是就修改数据库中的用户信息并返回
            R r = memberFeignService.socialLogin(socialUser);
            if (r.getCode() == 0) {
                // 调用成功
                MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>() {});
                System.out.println("登录成功: " + memberRespVo);
                // 将用户登录信息存到session中，使用了SpringSession后，自动将数据保存到 redis 中。
                // TODO： 需要扩展域名范围的。如果只在 auth.gulimall.com 域名下存储，那么其他域名访问不到 session 数据
                session.setAttribute("loginUser",memberRespVo);

                return "redirect:http://gulimall.com";
            }else {
                return  "redirect:http://auth.gulimall.com/login.html";
            }

        }else{
            // 请求失败
            return  "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
