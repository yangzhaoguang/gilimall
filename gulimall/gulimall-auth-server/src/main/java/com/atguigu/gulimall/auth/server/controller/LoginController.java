package com.atguigu.gulimall.auth.server.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.server.feign.MemberFeignService;
import com.atguigu.gulimall.auth.server.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.server.vo.UserLoginVo;
import com.atguigu.gulimall.auth.server.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 9:19
 * Description: 
 */
@Controller
public class LoginController {

/*    @RequestMapping({"login.html","/login"})
    public String login() {
        return  "login";
    }

    @RequestMapping("reg.html")
    public String reg() {
        return  "reg";
    }*/
    @Autowired
    private ThirdPartyFeignService thirdPartyService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 发送验证码
     * */
    @ResponseBody
    @RequestMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO: 1、接口防刷
        // 2、防止再次发送验证码
        String redisCode = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            // 判断存入code的时间与当前时间，如果相差小于60s 就不允许再次发送验证码
            Long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000 ) {
                return R.error(BizCodeEnum.VALID_CODE_EXCEPTION.getCode(), BizCodeEnum.VALID_CODE_EXCEPTION.getMessage());
            }
        }

        // 随机生产四位数字验证码
        String code = String.valueOf(new Random().nextInt(9000) + 1000) ;
        // 放入redis,并加上时间戳,并设置过期时间
        redisTemplate.opsForValue()
                .set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,code + "_" +System.currentTimeMillis(),5, TimeUnit.MINUTES);
        System.out.println("验证码: " + code);
        // 发送验证码
        thirdPartyService.sendCode(phone,code);
        return R.ok();
    }

    /**
     * TODO: 重定向携带数据使用session，但是在分布式系统下还有一些问题....
     * 注册
     * */
    @PostMapping("/register")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes attributes) {

        HashMap<String, String> errorMap = new HashMap<>();
        // 1、校验参数格式，并将错误信息封装到map中
        if (result.hasErrors()) {
            for (FieldError fieldError : result.getFieldErrors()) {
                    errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            // 将错误信息保存到 session域中
            // addFlashAttribute 只需要取出来一次，刷新页面就没有了
            attributes.addFlashAttribute("errors",errorMap);
            // 重定向到注册页面
            return  "redirect:http://auth.gulimall.com/reg.html";
        }

        // 2、判断验证码是否一致
        String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());


        if (!StringUtils.isEmpty(code)) {
            if (userRegistVo.getCode().equalsIgnoreCase(code.split("_")[0])){
                // 验证码一致
                // 删除redis中的验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
                // 远程调用，将信息保存到数据库中
                R r = memberFeignService.regist(userRegistVo);
                if (r.getCode() == 0) {
                    // 调用成功
                    return  "redirect:http://auth.gulimall.com/login.html";
                }else{
                    // 将错误消息放到map集合
                    errorMap.put("msg",(String) r.get("msg"));
                    attributes.addFlashAttribute("errors",errorMap);
                    // 调用失败
                    return  "redirect:http://auth.gulimall.com/reg.html";
                }
            }else{
                errorMap.put("code","验证码错误");
                return "reg";
            }
        }else {
            // 验证码过期
            errorMap.put("code"," 验证码过期");
            attributes.addFlashAttribute("errors",errorMap);
            return  "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    /**
     * 登录
     * */
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {
        // 远程调用登录
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            // 存入session中
            MemberRespVo memberRespVo = login.getData("data", new TypeReference<MemberRespVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER,memberRespVo);
            // 登录成功
            return  "redirect:http://gulimall.com";
        }else {
            // 登录失败，保存错误信息
            HashMap<String, String> errorsMap = new HashMap<>();
            errorsMap.put("msg", (String) login.get("msg"));
            attributes.addFlashAttribute("errors",errorsMap);
            return  "redirect:http://auth.gulimall.com/login.html";
        }

    }

    /**
     * 自定义跳转到login页面逻辑
     * */
    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        // 判断是否登录
        return session.getAttribute(AuthServerConstant.LOGIN_USER) == null ? "login" : "redirect:http://gulimall.com";
    }
}
