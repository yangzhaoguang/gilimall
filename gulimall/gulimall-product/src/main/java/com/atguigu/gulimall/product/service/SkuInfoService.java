package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);


    PageUtils queryPageByCondition(Map<String, Object> params);


    /**
     * @description
     * @date 2023/1/25 17:28
     * @param skuId
     * @return com.atguigu.gulimall.product.com.atguigu.common.vo.SkuItemVo
     * 根据 skuId 封装商品详情
     */
    SkuItemVo item(Long skuId);
}

