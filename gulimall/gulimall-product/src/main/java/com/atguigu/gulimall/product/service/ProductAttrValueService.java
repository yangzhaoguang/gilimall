package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities);


    List<ProductAttrValueEntity> listBaseAttrForSpu(Long spuId);


    void updateBaseAttrForSpu(Long spuId, List<ProductAttrValueEntity> spuAttrList);
}

