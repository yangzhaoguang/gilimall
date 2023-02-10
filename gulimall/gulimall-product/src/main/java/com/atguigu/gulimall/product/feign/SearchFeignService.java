package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Handsome Man.
 *
 * Author: YZG
 * Date: 2023/1/12 22:08
 * Description: 
 */
@FeignClient("gulimall-seartch")
public interface SearchFeignService {

    /*
    * 向 ES 发送请求，保存商品上架信息
    * */
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
