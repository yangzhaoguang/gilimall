package com.atguigu.gulimall.seartch.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.seartch.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.seartch.constant.EsConstant;
import com.atguigu.gulimall.seartch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Author: YZG
 * Date: 2023/1/12 21:50
 * Description: 
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /*
    * 商品上架，将商品信息保存到 ES 中
    * */
    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        // 1、增加索引
        // BulkRequest bulkRequest
        BulkRequest bulkRequest = new BulkRequest(EsConstant.PRODUCT_INDEX);
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 构建批量请求
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.id(skuEsModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        // 批量请求
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // 没有错误返回 false，有错误返回 true
        boolean hasFailures = bulk.hasFailures();
        if (hasFailures) {
            BulkItemResponse[] items = bulk.getItems();
            List<Integer> itemIds = Arrays.stream(items).map(BulkItemResponse::getItemId).collect(Collectors.toList());
            log.error("商品上架出现异常: {}", itemIds);
        }

        return hasFailures  ;
    }
}
