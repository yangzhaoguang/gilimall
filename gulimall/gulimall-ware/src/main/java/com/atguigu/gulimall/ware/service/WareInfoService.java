package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.LockStockResult;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-15 00:03:54
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/2/2 10:27
     * @param addrId
     * @return java.math.BigDecimal 根据收货地址计算运费
     */
    FareVo getFare(Long addrId);


}

