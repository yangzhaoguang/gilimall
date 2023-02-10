package com.atguigu.gulimall.ware.exception;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 20:58
 * Description: 
 */
public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(Long skuId) {
        super(skuId + " 商品,没有足够的库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
