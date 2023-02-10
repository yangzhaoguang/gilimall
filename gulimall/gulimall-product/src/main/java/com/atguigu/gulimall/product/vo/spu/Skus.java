/**
 * Copyright 2023 bejson.com
 */
package com.atguigu.gulimall.product.vo.spu;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2023-01-04 21:50:28
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Skus {

    private List<Attr> attr;

    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;

    private List<Images> images;

    private List<String> descar;

    private Integer fullCount;
    private BigDecimal discount;


    private int countStatus;

    private BigDecimal fullPrice;
    private BigDecimal reducePrice;

    private int priceStatus;

    private List<MemberPrice> memberPrice;


}