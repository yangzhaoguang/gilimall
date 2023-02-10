package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 商品属性
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * spu规格参数维护——获取spu规格
     * GET /product/attr/base/listforspu/{spuId}
     */
    @GetMapping("/base/listforspu/{spuId}")
    //@RequiresPermissions("com.atguigu.gulimall.product:attr:list")
    public R listForSpu(@PathVariable("spuId") Long  spuId) {
       List<ProductAttrValueEntity> productAttrValueEntity = productAttrValueService.listBaseAttrForSpu(spuId);
        return R.ok().put("data", productAttrValueEntity);
    }


    /**
     *  属性规格参数查询——关联分组名称、分类名称
     * @param params 封装前端请求参数
     * @param catelogId 分类ID
     * @param type base 基础属性，sale 销售属性
     * @return
     */
    //  /product/attr/base/list/{catelogId}
    // /product/attr/sale/list/{catelogId}
    @GetMapping("/{type}/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params,
                      @PathVariable("catelogId") Long catelogId,
                      @PathVariable("type") String type) {
        PageUtils page = attrService.queryBaseListPage(params, catelogId, type);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("com.atguigu.gulimall.product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("com.atguigu.gulimall.product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        // AttrEntity attr = attrService.getById(attrId);
        AttrRespVo vo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", vo);
    }

    /**
     * 保存
     * 新增规格参数并关联分组
     */
    @RequestMapping("/save")
    // @RequiresPermissions("com.atguigu.gulimall.product:attr:save")
    public R save(@RequestBody AttrVo attrVo) {
        // attrService.save(attr);
        attrService.saveAttr(attrVo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("com.atguigu.gulimall.product:attr:update")
    public R update(@RequestBody AttrVo attrVo) {

        attrService.updateAttr(attrVo);
        return R.ok();
    }


    /**
     * spu规格参数维护——修改spu规格
     * /product/attr/update/{spuId}
     */
    @PostMapping("/update/{spuId}")
    // @RequiresPermissions("com.atguigu.gulimall.product:attr:update")
    public R update(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> spuAttrList) {

        productAttrValueService.updateBaseAttrForSpu(spuId,spuAttrList);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("com.atguigu.gulimall.product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
