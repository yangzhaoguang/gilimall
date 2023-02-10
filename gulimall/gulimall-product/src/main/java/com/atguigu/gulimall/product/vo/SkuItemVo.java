package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品详情Vo
 * Author: YZG
 * Date: 2023/1/25 15:30
 * Description: 
 */
@Data
public class SkuItemVo {

    // 1、Sku 的基本信息：对应的表 `pms_sku_info`
    private SkuInfoEntity info;
    // 2、Sku 的图片信息：对应的表 `pms_sku_images`
        private List<SkuImagesEntity> images;
    // 3、Sku 的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttrs;
    // 4、商品介绍信息：对应的表`pms_spu_info_desc`
    private SpuInfoDescEntity desc;
    // 5、商品的规格参数：一个属性分组下对应多个属性
    private List<SpuItemAttrGroupVo> groupAttrs;
    // 是否有货
    private boolean HasStock = true;
    // 商品秒杀信息
    private SeckillInfoVo seckillInfo;


    /**
     * 商品的销售属性信息
     * */
    @Data
    public static class SkuItemSaleAttrVo {
        /**
         * 属性id
         */
        private Long attrId;
        /**
         * 属性名
         */
        private String attrName;
        /**
         * 属性值
         * */
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    /**
     * 属性值与 skuid 的对应关系
     * */
    @Data
    public static class AttrValueWithSkuIdVo{

        private String attrValue;
        private String skuIds;
    }

    /**
     * 分组信息
     * */
    @Data
    public static class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttr> attrs;
    }

    /**
    * 分组下的基本属性信息
    * */
    @Data
    public static class SpuBaseAttr {
        private String attrName;
        private String attrValue;
    }

}
