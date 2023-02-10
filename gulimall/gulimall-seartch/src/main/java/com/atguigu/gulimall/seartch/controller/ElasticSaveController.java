package com.atguigu.gulimall.seartch.controller;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seartch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/12 21:47
 * Description:  保存索引
 */
@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {

        // 返回上架状态
        Boolean b = false;
        try {
            // 如果没有错误返回 false ，有错误返回 true
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            // 上架失败
            e.printStackTrace();
           return R.error(BizCodeEnum.PRODUCT_UP.getCode(), BizCodeEnum.PRODUCT_UP.getMessage());
        }


        return b ? R.error(BizCodeEnum.PRODUCT_UP.getCode(), BizCodeEnum.PRODUCT_UP.getMessage()) : R.ok();
    }
}
