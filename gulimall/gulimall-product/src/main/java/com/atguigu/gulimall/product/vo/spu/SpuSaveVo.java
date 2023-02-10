/**
  * Copyright 2023 bejson.com 
  */
package com.atguigu.gulimall.product.vo.spu;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2023-01-04 21:50:28
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    /**
     * 商品名称
     * */
    private String spuName;
    /**
     * 商品描述
     * */
    private String spuDescription;
    /**
     * 分类ID
     * */
    private Long catelogId;
    /**
     * 品牌ID
     * */
    private Long brandId;
    /**
     * 手机重量
     * */
    private BigDecimal weight;
    /**
     * 上架状态[0 - 下架，1 - 上架]
     * */
    private int publishStatus;
    /**
     * 商品介绍
     * */
    private List<String> decript;
    /**
     * 商品图集
     * */
    private List<String> images;

    /**
     * 商品积分
     * */
    private Bounds bounds;

    /**
     * 基本属性
     * */
    private List<BaseAttrs> baseAttrs;

    /**
     * 销售属性
     * */
    private List<Skus> skus;



}