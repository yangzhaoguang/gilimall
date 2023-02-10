package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.AttrRelaVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);


    PageUtils queryPage(Map<String, Object> params, Long catelogId);



    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsWithCatelogId(Long catelogId);

    /**
     * @description
     * @date 2023/1/25 16:49
     * @param spuId 商品id
     * @param catelogId 分类id
     * @return java.util.List<com.atguigu.gulimall.product.com.atguigu.common.vo.SkuItemVo.SpuItemAttrGroupVo>
     *     根据商品id、分类id查询分组以及分组下的所有属性信息
     */
    List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catelogId);
}

