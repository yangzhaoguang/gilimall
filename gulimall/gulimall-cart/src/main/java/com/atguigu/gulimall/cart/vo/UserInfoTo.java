package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 *
 * Author: YZG
 * Date: 2023/1/30 17:17
 * Description: 
 */
@Data
@ToString
public class UserInfoTo {
    private Long userId; // 登录后的用户id
    private String userKey;
    // 是否设置了 userKey
    private boolean isTempUser = false;
}
