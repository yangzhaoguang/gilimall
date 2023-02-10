package com.atguigu.gulimall.member.vo;

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
public class MemberRegistVo {

    private String userName;

    private String password;

    private String phone;

}
