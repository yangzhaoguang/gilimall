package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrRelaVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    // 新增规格参数并关联分组
    void saveAttr(AttrVo attrVo);

    // 查询规格参数列表
    PageUtils queryBaseListPage(Map<String, Object> params, Long catelogId, String type);

    // 修改之回显规格参数数据
    AttrRespVo getAttrInfo(Long attrId);

    // 修改规格参数
    void updateAttr(AttrVo attrVo);

    // 根据分组ID查询出关联的所有属性
    List<AttrEntity> getAttrsRelation(Long attrgroupId);

    // 删除与分组关联的属性
    void deleteBatch(AttrRelaVo[] relaVo);


    PageUtils getAttrsNoRelation(Map<String, Object> params, Long attrgroupId);


}

