package com.atguigu.gulimall.seartch.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 16:23
 * Description: 
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /*
    * 远程调用：根据属性id查询属性信息
    * */
    @RequestMapping("product/attr/info/{attrId}")
    public R getAttrById(@PathVariable("attrId") Long attrId);

    /*
     * 根据 品牌id 集合查询品牌信息
     * */
    @RequestMapping("product/brand/infos")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:info")
    public R getBrandsByIds(@RequestBody List<Long> brandIds) ;
}
