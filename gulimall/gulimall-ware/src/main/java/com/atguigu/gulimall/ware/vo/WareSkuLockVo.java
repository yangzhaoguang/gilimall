package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 19:12
 * Description: 
 */
@Data
public class WareSkuLockVo {

    // 订单号
    private String orderSn;

    // 需要锁定的库存信息
    private List<OrderItemVo> locks;
}
