package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/1/30 16:55
 * Description: 
 */
public interface CartService {
    /**
     * @description
     * @date 2023/1/30 19:58
     * @param skuId
     * @param num
     * @return com.atguigu.gulimall.cart.vo.CartItem
     * 增加商品到购物车
     */
    CartItem addToCart(Long skuId, Integer num);

    /**
     * @description
     * @date 2023/1/30 21:58
     * @param skuId
     * @return com.atguigu.gulimall.cart.vo.CartItem
     * 查询购物车中的商品
     */
    CartItem getCartItem(Long skuId);

    /**
     * @description
     * @date 2023/1/30 22:18
     * @param
     * @return com.atguigu.gulimall.cart.vo.Cart
     * 获取&合并购物车
     */
    Cart getCarts();

    /**
     * @description
     * @date 2023/1/31 8:34
     * @param cartKey
     * @return void 清空购物车
     */
    void clearCart(String cartKey);

    /**
     * @description
     * @date 2023/1/31 9:36
     * @param skuId
     * @param checked
     * @return void 勾选购物项
     */
    void checkItem(Long skuId, Integer checked);

    /**
     * @description
     * @date 2023/1/31 9:52
     * @param skuId
     * @param num
     * @return void 修改商品数量
     */
    void countItem(Long skuId, Integer num);

    /**
     * @description
     * @date 2023/1/31 10:07
     * @param skuId
     * @return void 删除购物项
     */
    void deleteItem(Long skuId);

    /**
     * @description
     * @date 2023/2/1 15:49
     * @param
     * @return java.util.List<com.atguigu.gulimall.cart.vo.CartItem> 查询用户购物车中所有勾选的商品
     */
    List<CartItem> currentUserCartItems();
}
