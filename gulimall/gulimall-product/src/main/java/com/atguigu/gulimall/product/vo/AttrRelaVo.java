package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/3 17:15
 * Description: 属性、分组关联
 */
@Data
public class AttrRelaVo {
    //     [{"attrId":1,"attrGroupId":2}]
    private Long attrId;
    private Long attrGroupId;
}
