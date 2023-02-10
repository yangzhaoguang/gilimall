package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.vo.BrandVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


     /**
     *  /product/categorybrandrelation/brands/list
      *  获取分类关联的品牌
     * */
     @GetMapping("/brands/list")
     public R categoryBrandRelationList(@RequestParam(value = "catId",required = true) Long catId) {
         // 业务处理
         List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);

         // 封装页面所需要的数据
         List<BrandVo> brandVos =  brandEntities.stream().map(item -> {
             BrandVo brandVo = new BrandVo();
             brandVo.setBrandId(item.getBrandId());
             brandVo.setBrandName(item.getName());
             return brandVo;
         }).collect(Collectors.toList());

         return  R.ok().put("data",brandVos);
     }


    /**
     * 列表
     * 查询品牌关联分类
     */
    @GetMapping("catelog/list")
    //@RequiresPermissions("com.atguigu.gulimall.product:categorybrandrelation:list")
    public R catelogList(@RequestParam Long brandId){
        // PageUtils page = categoryBrandRelationService.queryPage(params);

        List<CategoryBrandRelationEntity> data =
                categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
        return R.ok().put("data", data);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("com.atguigu.gulimall.product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("com.atguigu.gulimall.product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     * 新增品牌关联分类
     */
    @PostMapping("/save")
   // @RequiresPermissions("com.atguigu.gulimall.product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		// categoryBrandRelationService.save(categoryBrandRelation);
        categoryBrandRelationService.saveDetail(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
   // @RequiresPermissions("com.atguigu.gulimall.product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
   // @RequiresPermissions("com.atguigu.gulimall.product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
