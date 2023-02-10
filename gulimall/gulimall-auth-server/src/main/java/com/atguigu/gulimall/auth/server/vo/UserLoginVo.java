package com.atguigu.gulimall.auth.server.vo;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/28 9:06
 * Description: 登录vo
 */
@Data
public class UserLoginVo {

    private String loginacct;
    private String password;
}
