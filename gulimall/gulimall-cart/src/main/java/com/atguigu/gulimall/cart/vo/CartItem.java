package com.atguigu.gulimall.cart.vo;

import jdk.nashorn.internal.objects.annotations.Function;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/30 15:29
 * Description: 
 */

public class CartItem {
    /*
    * 	skuId: 2131241,
    * check: true,   // 表示商品是否被勾选上
    * title: "Apple iphone.....",
    * defaultImage: "...",
    * price: 4999,
    * count: 1,
    * totalPrice: 4999，
    * skuSaleVO: {...} // 商品的销售属性集合
    * */
    private Long  skuId;
    private boolean check = true;
    private String title;
    private String defaultImage;
    private BigDecimal price ;
    private Integer count = 1;
    private BigDecimal totalPrice ;
    private List<String> skuAttr;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /*
    * 计算出商品价格
    * */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.count));
    }


    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }


}
