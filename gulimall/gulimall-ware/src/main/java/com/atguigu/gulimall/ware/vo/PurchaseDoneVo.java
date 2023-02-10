package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/7 15:42
 * Description: 
 */
@Data
public class PurchaseDoneVo {
    //    id: 123,//采购单id
    //    items: [{itemId:1,status:4,reason:""}] //完成/失败的需求详情
    private Long id ;
    private List<PurchaseDoneItemVo> items ;
}
