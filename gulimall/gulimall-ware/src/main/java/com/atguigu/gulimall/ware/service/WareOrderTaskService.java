package com.atguigu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-15 00:03:54
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/2/5 16:00
     * @param orderSn
     * @return com.atguigu.gulimall.ware.entity.WareOrderTaskEntity 根据订单号查询工作单
     */
    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

