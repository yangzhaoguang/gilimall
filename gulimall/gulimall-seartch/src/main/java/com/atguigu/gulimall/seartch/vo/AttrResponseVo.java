package com.atguigu.gulimall.seartch.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 16:18
 * Description: 
 */
@Data
public class AttrResponseVo {

    /**
     * id
     */
    private Long id;
    /**
     * 商品id
     */
    private Long spuId;
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
     */
    private String attrValue;
    /**
     * 顺序
     */
    private Integer attrSort;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】
     */
    private Integer quickShow;

    // catelogName/groupName
    // 分类名称
    private String catelogName;

    // 分组名称
    private String groupName;

    // 分类完整路径
    private Long[] catelogPath;
}
