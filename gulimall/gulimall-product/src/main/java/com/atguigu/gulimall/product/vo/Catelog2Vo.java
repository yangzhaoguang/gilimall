package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 首页展示 三级分类
 * Author: YZG
 * Date: 2023/1/18 17:01
 * Description: 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {
    // 一级分类 id
    private String catalog1Id;
    // 三级分类集合
    private List<Catelog3Vo> catalog3List;
    // 二级分类id
    private String id;
    // 二级分类名字
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
   public static class Catelog3Vo{
        // 二级分类id
        private String catalog2Id;
        // 三级分类 id
        private String id;
        // 三级分类名字
        private String name;
    }
    /*
     *
     *   "1": [
     * {
     *   "catalog1Id": "1",
     *   "catalog3List": [
     *     {
     *       "catalog2Id": "1",
     *       "id": "1",
     *       "name": "电子书"
     *     }
     *   ],
     *   "id": "1",
     *   "name": "电子书刊"
     * },
     * */
}

