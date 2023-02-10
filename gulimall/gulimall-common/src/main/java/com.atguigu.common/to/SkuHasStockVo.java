package com.atguigu.common.to;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/1/12 20:57
 * Description: 
 */
@Data
public class SkuHasStockVo {
    private Long  skuId;
    private Boolean hasStock;
}
