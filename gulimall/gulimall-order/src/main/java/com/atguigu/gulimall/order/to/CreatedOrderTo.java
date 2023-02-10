package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 15:50
 * Description:
 */
@Data
public class CreatedOrderTo {

    // 订单项
    private List<OrderItemEntity> items;

    // 订单信息
    private OrderEntity order;

    // 订单计算的应付价格
    private BigDecimal payPrice;

    // 运费
    private BigDecimal fare;
}
