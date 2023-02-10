package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-15 00:03:54
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void addStock(Long skuId, Long wareId, Integer skuNum);


    HashMap<Long, Boolean> getSkusHasStock(List<Long> skuIds);

    /**
     * @description
     * @date 2023/2/2 19:25
     * @param vo
     * @return java.util.List<com.atguigu.gulimall.ware.vo.LockStockResult> 锁定库存
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * @description
     * @date 2023/2/5 11:30
     * @param to
     * @return void 自动解锁库存
     */
    void unLock(StockLockedTo to);

    /**
     * @description
     * @date 2023/2/5 15:58
     * @param orderEntity
     * @return void 订单关闭，即将解锁库存
     */
    void unLock(OrderTo orderEntity);
}

