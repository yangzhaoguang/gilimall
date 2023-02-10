package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 14:58
 * Description: 接收从订单确认页的信息，用来提交订单
 */
@Data
public class OrderSubmitVo {
    // 收货地址id
    private Long  addrId;
    // 支付类型。目前只使用在线支付
    private Integer patType;

    // 商品信息无需在这里封装，在购物车中重新查询一次

    // 用户信息可以直接从 session 中获取

    // 应付金额 —— 验证价格
    private BigDecimal payPrice;
    // 防重令牌
    private String orderToken;

    // 备注
    private String notes;
}
