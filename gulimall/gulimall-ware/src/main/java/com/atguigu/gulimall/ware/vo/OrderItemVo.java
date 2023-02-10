package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 15:17
 * Description: 
 */
@Data
public class OrderItemVo {
    private Long  skuId;
    private String title;
    private String defaultImage;
    private BigDecimal price ;
    private Integer count = 1;
    private BigDecimal totalPrice ;
    private List<String> skuAttr;
    // TODO 是否有货
    private boolean hasStock;
    private Double weight;
}
