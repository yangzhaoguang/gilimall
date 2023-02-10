package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/2 21:01
 * Description: 查询规格参数列表
 */
@Data
public class AttrRespVo extends  AttrVo{
    // catelogName/groupName
    // 分类名称
    private String catelogName;

    // 分组名称
    private String groupName;

    // 分类完整路径
    private Long[] catelogPath;
}
