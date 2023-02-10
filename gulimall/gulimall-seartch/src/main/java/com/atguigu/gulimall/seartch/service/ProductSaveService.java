package com.atguigu.gulimall.seartch.service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/1/12 21:50
 * Description: 
 */
@Service
public interface ProductSaveService {


    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
