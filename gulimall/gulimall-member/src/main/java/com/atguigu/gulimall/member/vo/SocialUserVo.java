package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/29 11:01
 * Description: 
 */
@Data
public class SocialUserVo {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;

}
