package com.atguigu.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * Author: YZG
 * Date: 2023/1/29 15:07
 * Description: 
 */
@Data
public class MemberRespVo implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 会员等级id
     */
    private Long levelId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 头像
     */
    private String header;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 生日
     */
    private Date birth;
    /**
     * 所在城市
     */
    private String city;
    /**
     * 职业
     */
    private String job;
    /**
     * 个性签名
     */
    private String sign;
    /**
     * 用户来源
     */
    private Integer sourceType;
    /**
     * 积分
     */
    private Integer integration;
    /**
     * 成长值
     */
    private Integer growth;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 注册时间
     */
    private Date createTime;

    /**
     * 访问令牌
     * */
    private String accessToken;
    /**
     * 访问令牌失效时间
     * */
    private long expiresIn;

    /**
     * 用户唯一标识
     * */
    private String socialUid;
}
