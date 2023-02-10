package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 16:01
 * Description: 
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare ;
}
