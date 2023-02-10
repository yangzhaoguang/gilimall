package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.swing.*;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 15:15
 * Description: 订单确认页Vo
 */
// @Data
    @ToString
public class OrderConfirmVo {

    // 收货地址
    @Getter @Setter
    List<MemberAddressVo> address;

    // 订单购物项
    @Getter @Setter
    List<OrderItemVo> items;

    // 会员积分
    @Getter @Setter
    Integer integration;

    // 订单总金额
    BigDecimal total ;

    // 订单支付的总金额
    BigDecimal payPrice;

    // 订单令牌：防止重复提交订单
    @Getter @Setter
    String orderToken;

    // 共几件商品
    Integer count;

    // 商品是否有库存
    @Getter @Setter
    Map<Long,Boolean> stocks;

    // 订单总金额  = 每个购物项价格累加起来
    // 每个购物项的价格 = 每个商品价格 * 商品数量
    public BigDecimal getTotal() {
        return (items != null && items.size() > 0)
                ? items.stream().map(item ->item.getPrice().multiply(new BigDecimal(item.getCount()))).reduce(BigDecimal::add).get()
                : new BigDecimal(0);
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public Integer getCount() {

        return (items != null && items.size() > 0)
                ? items.stream().map(OrderItemVo::getCount).reduce(Integer::sum).get()
                : 0;
    }
}
