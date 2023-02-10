package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.vo.spu.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void saveSpuInfo(SpuSaveVo spuSaveVo);


    void saveSpuInfoBase(SpuInfoEntity spuInfoEntity);


    PageUtils queryPageByCondition(Map<String, Object> params);


    void up(Long spuId);

    /**
     * @description
     * @date 2023/2/2 16:52
     * @param skuId
     * @return com.atguigu.gulimall.product.entity.SkuInfoEntity 根据 skuId 获取 SpuInfo 信息
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

