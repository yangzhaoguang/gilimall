package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/1/25 17:17
     * @param spuId 商品id
     * @return java.util.List<com.atguigu.gulimall.product.com.atguigu.common.vo.SkuItemVo.SkuItemSaleAttrVo>
     *     根据商品ID查询出所有的销售属性名以及属性值
     */
    List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuID(Long spuId);

    /**
     * @description
     * @date 2023/1/30 20:48
     * @param skuId
     * @return java.util.List<java.lang.String>
     *     根据skuId查询商品销售属性名，以及销售属性值，并封装成 List
     * [
     *  {可选版本:8+128G},
     *  {颜色:冰霜银}
     * ]
     */
    List<String> getSkuAttrValueAsStringList(Long skuId);
}

