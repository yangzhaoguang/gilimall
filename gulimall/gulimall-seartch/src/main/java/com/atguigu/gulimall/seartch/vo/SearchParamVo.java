package com.atguigu.gulimall.seartch.vo;

import lombok.Data;

import java.util.List;

/**
 * 检索条件模型
 * Author: YZG
 * Date: 2023/1/21 22:34
 * Description:
 */
@Data
public class SearchParamVo {

    // 关键字搜索
    private String keyword;

    // 分类id
    private Long catalog3Id;

    /*
     * 排序条件
     *  saleCount(销量)、hotScore(热度)、skuPrice(价格)
     *   sort=saleCount_asc/desc
     *   sort=hotScore_asc/desc
     *   sort=skuPrice_asc/desc
     * */
    private String sort;

    /*
     * 过滤条件
     * hasStock(是否有库存)、skuPrice(价格区间)、brandId(品牌)、attrs(基本属性)
     *  hasStock=0/1  0表示未勾选[仅显示有货] 1表示勾选了[仅显示有货]
     *  skuPrice=1_200/_200/200_  表示: 1~100 、~200、200~
     *   brandId=1&brandId=2  可能会选中多个品牌
     *   attrs=1_其他  属性也可能勾选多个，1_其他：id为1的属性值为其他
     * */

    public Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    // 页码数
    private Integer pageNum = 1;

    // URL 中原生的参数字符串
    private String _queryString;

    /*
     * 完整的参数:
     *  keyword=小米&sort=saleCount_desc/asc&hasStock=0/1&skuPrice=400_1900&brandId=1 &catalogId=1&attrs=1_3G:4G:5G&attrs=2_骁龙 845&attrs=4_高清屏
     * */

}
