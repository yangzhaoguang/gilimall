package com.atguigu.gulimall.auth.server.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 19:00
 * Description:  用户注册Vo
 */
@Data
public class UserRegistVo {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6,max = 18,message = "用户名长度必须为: 6~18 位")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码长度必须为: 6~18 位")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}",message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code ;
}
