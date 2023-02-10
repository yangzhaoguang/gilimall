package com.atguigu.gulimall.order.service;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 23:53:13
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @description
     * @date 2023/2/1 15:22
     * @param
     * @return com.atguigu.gulimall.order.vo.OrderConfirmVo 订单确认页面的显示数据
     */
    OrderConfirmVo orderConfirm() throws ExecutionException, InterruptedException;

    /**
     * @description
     * @date 2023/2/2 15:16
     * @param vo
     * @return com.atguigu.gulimall.order.vo.OrderSubmitResponseVo 提交订单
     */
    OrderSubmitResponseVo submitOrder(OrderSubmitVo vo) throws NoStockException;

    /**
     * @description
     * @date 2023/2/5 8:30
     * @param orderSn
     * @return com.atguigu.gulimall.order.entity.OrderEntity 根据订单号查询订单
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * @description
     * @date 2023/2/5 15:01
     * @param order
     * @return void 关闭订单
     */
    void closeOrder(OrderEntity order);

    /**
     * @description
     * @date 2023/2/5 20:17
     * @param orderSn
     * @return com.atguigu.gulimall.order.vo.PayVo 查询支付信息
     */
    PayVo getOrderPay(String orderSn);

    /**
     * @description
     * @date 2023/2/5 21:52
     * @param params
     * @return com.atguigu.common.utils.PageUtils 查询登录用户的所有订单
     */
    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * @description
     * @date 2023/2/6 10:44
     * @param vo
     * @return java.lang.String 处理支付成功的异步回调
     */
    String handlePayResult(PayAsyncVo vo);

    /**
     * @description
     * @date 2023/2/8 16:23
     * @param orderTo
     * @return void 创建秒杀订单
     */
    void createSeckillOrder(SeckillOrderTo orderTo);
}

