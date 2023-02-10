package com.atguigu.gulimall.cart.vo;

import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * Author: YZG
 * Date: 2023/1/30 15:29
 * Description:  购物车
 */

public class Cart {

    private List<CartItem> items;  // 购物车
    private Integer CountNum ; 	// 商品总数量
    private Integer CountType ; // 商品类型数量
    private BigDecimal totalAmount; // 商品总价
    private BigDecimal reduce = new BigDecimal(0); // 优惠减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    /*
    * 计算商品的总数量
    * */
    public Integer getCountNum() {

        return items != null && items.size() > 0 ? items.stream().map(CartItem::getCount).reduce(Integer::sum).get() : 0;
    }


    /*
    * 计算商品的类型
    * */
    public Integer getCountType() {
        return items != null && items.size() > 0 ? items.size(): 0;
    }


    /*
    * 计算购物车中商品的总价格
    *       每件商品的总和 - 优惠价格
    * */
    public BigDecimal getTotalAmount() {

        // 只有勾选的商品,才参与计算总价格
        return  items != null && items.size() > 0
                ? items.stream().filter(CartItem::isCheck).map(CartItem::getTotalPrice).reduce(BigDecimal::add).get().subtract(this.reduce)
                : new BigDecimal(0);
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
