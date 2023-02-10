package com.atguigu.gulimall.auth.server.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.server.vo.SocialUserVo;
import com.atguigu.gulimall.auth.server.vo.UserLoginVo;
import com.atguigu.gulimall.auth.server.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 20:59
 * Description: 
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    /**
     * 登录
     * */
    @PostMapping("member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    /**
     * 社交登录
     * */
    @PostMapping("member/member/sociallogin")
    public R socialLogin(@RequestBody SocialUserVo vo);
}
