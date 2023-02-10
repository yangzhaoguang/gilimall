package com.atguigu.gulimall.order.vo;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 19:15
 * Description:  锁定库存结果vo
 */
@Data
public class LockStockResult {
    private Long skuId;
    // 锁定的数量
    private Integer num ;
    // 是否锁定成功
    private boolean locked;
}
