package com.atguigu.gulimall.cart;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// @SpringBootTest
public class GulimallCartApplicationTests {

    @Test
    public void contextLoads() {
        CartItem cartItem = new CartItem();
        cartItem.setPrice(new BigDecimal(99));
        CartItem cartItem1 = new CartItem();
        cartItem1.setPrice(new BigDecimal(99));


        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        cartItems.add(cartItem1);

        Cart cart = new Cart();
        cart.setItems(cartItems);

        System.out.println(cart.getTotalAmount());
    }

}
