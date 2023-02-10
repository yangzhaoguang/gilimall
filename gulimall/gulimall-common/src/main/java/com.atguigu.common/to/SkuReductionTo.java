package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/5 22:38
 * Description:  优惠信息
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private Integer fullCount;
    private BigDecimal discount;


    private int countStatus;

    private BigDecimal fullPrice;
    private BigDecimal reducePrice;

    private int priceStatus;

    private List<MemberPrice> memberPrice;
}
