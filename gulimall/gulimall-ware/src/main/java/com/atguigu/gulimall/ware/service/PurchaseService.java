package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-15 00:03:54
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);


    PageUtils queryPageByunreceive(Map<String, Object> params);


    void mergePurchase(MergeVo mergeVo);


    void receivedPurchase(List<Long> purchaseIds);


    void donePurchase(PurchaseDoneVo purchaseDoneVo);
}

