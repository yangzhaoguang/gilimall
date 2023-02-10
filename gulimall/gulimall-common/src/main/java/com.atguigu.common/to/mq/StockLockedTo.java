package com.atguigu.common.to.mq;

import lombok.Data;

/**
 *
 * Author: YZG
 * Date: 2023/2/4 22:07
 * Description: 
 */
@Data
public class StockLockedTo {
    private Long taskId ;// 工作单id
    private StockDetailLockedTo taskDetail; // 工作单详情
}
