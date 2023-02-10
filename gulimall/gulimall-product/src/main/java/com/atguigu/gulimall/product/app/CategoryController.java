package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.R;



/**
 * 商品三级分类
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类，封装成树形结构
     */
    @RequestMapping("/list/tree")
    //@RequiresPermissions("com.atguigu.gulimall.product:category:list")
    public R list(@RequestParam Map<String, Object> params){

        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
   // @RequiresPermissions("com.atguigu.gulimall.product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   // @RequiresPermissions("com.atguigu.gulimall.product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }
    /**
     * 批量修改
     */
    @RequestMapping("/update/sort")
    // @RequiresPermissions("com.atguigu.gulimall.product:category:update")
    public R updateSort(@RequestBody CategoryEntity[] category){
        // categoryService.updateById(category);
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
   // @RequiresPermissions("com.atguigu.gulimall.product:category:update")
    public R update(@RequestBody CategoryEntity category){
		// categoryService.updateById(category);
        // 级联更新
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     * 删除需要判断是否分类还在别的地方引用
     * @RequestBody: 获取请求体中的内容，只能发送 POST请求
     */
    @RequestMapping("/delete")
   // @RequiresPermissions("com.atguigu.gulimall.product:category:delete")
    public R delete(@RequestBody Long[] catIds){
        categoryService.removeMenuByIds(Arrays.asList(catIds));
		// categoryService.removeByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
