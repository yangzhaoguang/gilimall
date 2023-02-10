package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * Author: YZG
 * Date: 2023/2/2 10:44
 * Description: 运费
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare ;
}
