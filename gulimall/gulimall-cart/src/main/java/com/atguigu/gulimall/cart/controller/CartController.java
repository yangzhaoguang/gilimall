package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/30 16:56
 * Description: 
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;


    /*
    * 获取用户所有勾选的购物项
    * */
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getUserCartItems() {
        return cartService.currentUserCartItems();
    }


    @RequestMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId) {
        cartService.deleteItem(skuId);
        return  "redirect:http://cart.gulimall.com/cartList.html";
    }

    /**
     * 改变商品数量
     * */
    @RequestMapping("/countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer num){
        cartService.countItem(skuId,num);
        return  "redirect:http://cart.gulimall.com/cartList.html";
    }


    /**
     * 选中商品项
     * */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("checked")Integer checked){
        cartService.checkItem(skuId,checked);
        return  "redirect:http://cart.gulimall.com/cartList.html";
    }
    /**
     * 获取&合并购物车
     * */
    @GetMapping("/cartList.html")
    public String cartPage(Model model) {

        Cart cart = cartService.getCarts();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 增加商品到购物车
     *  RedirectAttributes
     *      addAttribute 将数据放到 url 后边
     *      addFlashAttribute 将数据放到session中，但是页面只能取出来一次，随后就清除
     * */
    @GetMapping("/addToCart")
    public String addToCart(
            @RequestParam("skuId") Long skuId,
            @RequestParam("num") Integer num,
            RedirectAttributes attributes) {

        CartItem cartItem = cartService.addToCart(skuId,num);
        attributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /*
    * 重新查询购物车中商品的详情
    *   防止刷新页面重复增加购物车
    * */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId")Long skuId, Model model){
        // 重新查询一遍商品
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",cartItem);
        return "success";
    }
}
