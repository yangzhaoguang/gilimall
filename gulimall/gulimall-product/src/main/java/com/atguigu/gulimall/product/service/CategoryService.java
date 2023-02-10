package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    // 查询所有分类，封装成树形结构
    List<CategoryEntity> listWithTree();

    // 删除菜单
    void removeMenuByIds(List<Long> asList);


    Long[] findCatelogPath(Long catelogId);

    // 级联更新
    void updateCascade(CategoryEntity category);


    List<CategoryEntity> getLevelOne();


    Map<String, List<Catelog2Vo>> getCatelog2Vo();
}

