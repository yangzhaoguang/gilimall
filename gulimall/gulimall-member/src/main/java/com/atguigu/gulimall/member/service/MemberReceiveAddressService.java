package com.atguigu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 23:48:23
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/2/1 15:42
     * @param memberId
     * @return java.util.List<com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity> 获取用户的所有收货的地址
     */
    List<MemberReceiveAddressEntity> getMemberAddresses(Long memberId);
}

