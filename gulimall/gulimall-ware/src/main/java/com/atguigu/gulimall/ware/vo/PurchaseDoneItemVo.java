package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/7 15:42
 * Description: 
 */
@Data
public class PurchaseDoneItemVo {
    //    itemId:1,status:4,reason:""
    private Long itemId;
    private Integer status;
    private String reason;
}
