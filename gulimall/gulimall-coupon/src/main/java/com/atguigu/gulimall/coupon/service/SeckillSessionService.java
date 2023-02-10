package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 23:41:59
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/2/7 11:34
     * @param
     * @return java.util.List<com.atguigu.gulimall.coupon.entity.SeckillSessionEntity>
     *     查询最近三天的秒杀场次
     */
    List<SeckillSessionEntity> findLatest3DaysSessions();
}

