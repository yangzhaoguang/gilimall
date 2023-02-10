package com.atguigu.gulimall.seartch.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/23 10:03
 * Description:  检索返回结果
 */
@Data
public class SearchResultVo {

    // 检索返回的所有商品信息
    private List<SkuEsModel> products;
    // 检索返回的所有品牌信息
    private List<BrandVo> brands;

    // 检索返回的所有分类信息
    private List<CatalogVo> catalogs;
    // 检索返回的所有分类信息
    private List<AttrsVo> attrs;

    // 当前页码
    private Integer pageNum;
    // 总记录数
    private Long total;
    // 总页码
    private Integer totalPages;
    // 页码集合
    private List<Integer> pageNavs ;

    // 面包屑导航数据=======================
    private List<NavVo> navs = new ArrayList<>() ;
    // 筛选了哪些属性，将这些属性的id
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private  String navName; // 属性名
        private  String navValue; // 属性值
        private  String navLink; // 链接地址

    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandImg;
        private String brandName;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrsVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
