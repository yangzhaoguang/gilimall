package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 *
 * Author: YZG
 * Date: 2023/1/30 16:55
 * Description: 
 */
@Service("cartService")
public class CartServiceImpl implements CartService {
    // 购物车前缀
    public static final String CART_TYPE_PREFIX = "gulimall:cart:";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        // 1、获取对应的购物车
        BoundHashOperations<String, Object, Object> cartOps = getCart();
        // 判断购物车中是否有新增加的商品
        String cartItemRedis = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(cartItemRedis)) {
            // 没有此商品
            CartItem cartItem = new CartItem();
            // 2、封装 sku 基本信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setSkuId(skuInfo.getSkuId());
                cartItem.setCheck(true);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setDefaultImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setCount(num);
            }, executor);

            // 3、封装 sku 销售属性
            CompletableFuture<Void> getSkuAttrsListTask = CompletableFuture.runAsync(() -> {
                List<String> skuAttrValueAsStringList = productFeignService.getSkuAttrValueAsStringList(skuId);
                cartItem.setSkuAttr(skuAttrValueAsStringList);
            }, executor);

            try {
                // 等待异步任务完成
                CompletableFuture.allOf(getSkuInfoTask,getSkuAttrsListTask).get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String cartJson = JSON.toJSONString(cartItem);
            // 将商品信息存入redis
            cartOps.put("" + skuId,cartJson);
            return cartItem;
        }
        else {
            // 有此商品，修改商品数量即可
            CartItem cartItem = JSON.parseObject(cartItemRedis, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }


    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCart();
        String cartItem = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(cartItem,CartItem.class);
    }


    @Override
    public Cart getCarts() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();

        if (userInfoTo.getUserId() != null) {
            // 登录：将用户购物车和临时购物车合并
            // 判断是否有临时购物车
            String tempCartKey = CART_TYPE_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCart = getCartItems(tempCartKey);
            if (tempCart != null && tempCart.size() > 0) {
                for (CartItem cartItem : tempCart) {
                    // 将临时购物车中的商品合并到用户购物车中
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
            }
            // 合并完成，重新查询用户购物车
            List<CartItem> cartItems = getCartItems(CART_TYPE_PREFIX + userInfoTo.getUserId());
            cart.setItems(cartItems);
            // 清空购物车
            clearCart(tempCartKey);
        }else {
            // 未登录
            // 查询临时购物车
            List<CartItem> cartItems = getCartItems(CART_TYPE_PREFIX + userInfoTo.getUserKey());
            cart.setItems(cartItems);
        }
        return cart;
    }

    /*
    * 清空购物车
    * */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }


    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cart = getCart();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked != 0);
        cart.put(skuId.toString(),JSON.toJSONString(cartItem));
        
    }


    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cart = getCart();
        cart.put(skuId.toString(), JSON.toJSONString(cartItem));
    }


    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cart = getCart();
        cart.delete(skuId.toString());
    }


    @Override
    public List<CartItem> currentUserCartItems() {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            String cartKey = CART_TYPE_PREFIX + userInfoTo.getUserId();
            // 获取购物车中所有的购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            // 过滤掉未勾选的商品。并且商品的价格应该从数据库中查询
            List<CartItem>  collect= cartItems.stream().filter(CartItem::isCheck).map(cartItem -> {
                BigDecimal price = productFeignService.getPrice(cartItem.getSkuId());
                cartItem.setPrice(price);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }else {
            return null;
        }
    }

    /*
    * 根据cartKey获取购物车中的所有商品
    * 用户：gulimall:cart:用户id
    * 临时: gulimall:cart:uuid
    * */
    private List<CartItem> getCartItems(String cartKey ) {

        // 获取购物车
        BoundHashOperations<String, Object, Object> cart = redisTemplate.boundHashOps(cartKey);
        // 获取购物车中所有的商品
        List<Object> cartItems = cart.values();
        if (cartItems != null && cartItems.size() > 0) {
            // 将购物车中的所有商品进行封装
            List<CartItem> items = cartItems.stream().map(item -> {
                String itemJson = (String) item;
                return JSON.parseObject(itemJson, CartItem.class);
            }).collect(Collectors.toList());

            return items;
        }
        return  null ;
    }

    /*
     * 获取对应的购物车
     * */
    private BoundHashOperations<String, Object, Object> getCart() {
        String cartKey = "";
        // 1、根据用户的登录状态，判断使用哪种购物车
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 登录状态、 `gulimall:cart: userId`
            cartKey = CART_TYPE_PREFIX + userInfoTo.getUserId();
        } else {
            // 未登录状态 gulimall:cart: userKey
            cartKey = CART_TYPE_PREFIX + userInfoTo.getUserKey();
        }
        // 绑定一个 hash，使用 boundHashOps 所有的操作都针对此 hash
        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(cartKey);
        return boundHashOps;
    }
}
