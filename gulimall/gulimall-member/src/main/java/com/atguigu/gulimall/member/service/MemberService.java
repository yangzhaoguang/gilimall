package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 23:48:22
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/1/27 20:58
     * @param vo
     * @return void
     * 注册
     */
    void regist(MemberRegistVo vo);

    /**
     * @description
     * @date 2023/1/27 21:07
     * @param userName
     * @return void
     * 判断用户名是否重复
     */
    void checkUserNameUnique(String userName) throws UserNameExistException;

    /**
     * @description
     * @date 2023/1/27 21:07
     * @param phone
     * @return void
     * 判断手机号是否重复
     */
    void checkPhoneUnique(String phone) throws PhoneExistException;

    /**
     * @description
     * @date 2023/1/28 9:09
     * @param vo
     * @return com.atguigu.gulimall.member.entity.MemberEntity
     * 普通登录
     */
    MemberEntity login(MemberLoginVo vo);

    /**
     * @description
     * @date 2023/1/29 11:35
     * @param vo
     * @return com.atguigu.gulimall.member.entity.MemberEntity
     * 社交登录——微博
     */
    MemberEntity socialLogin(SocialUserVo vo);
}

