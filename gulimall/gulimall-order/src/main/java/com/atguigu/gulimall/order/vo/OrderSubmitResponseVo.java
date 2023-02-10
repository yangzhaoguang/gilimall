package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 15:13
 * Description: 返回给订单支付页的响应信息
 */
@Data
public class OrderSubmitResponseVo {

    // 订单信息
    private OrderEntity order;

    // 订单提交失败 —— 错误状态码
    // 0 提交成功
    private Integer Code = 0;
}
