package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.impl.AttrAttrgroupRelationServiceImpl;
import com.atguigu.gulimall.product.service.impl.AttrServiceImpl;
import com.atguigu.gulimall.product.service.impl.CategoryServiceImpl;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.AttrRelaVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 属性分组
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private AttrServiceImpl attrService;

    @Autowired
    private AttrAttrgroupRelationServiceImpl attrAttrgroupRelationService;


    /**
     * 获取分类下的所有分组，以及每个分组的所有属性
     * /product/attrgroup/{catelogId}/withattr
     * */
    @GetMapping("{catelogId}/withattr")
    public R attrgroupWithAttrs(@PathVariable("catelogId") Long catelogId){
       List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos =  attrGroupService.getAttrGroupWithAttrsWithCatelogId(catelogId);
        return R.ok().put("data",attrGroupWithAttrsVos);
    }


    /**
     * 保存分组与属性关联关系
     * /product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R saveAttrRelation(@RequestBody List<AttrRelaVo> attrVo){
        attrAttrgroupRelationService.saveAttrRelation(attrVo);

        return R.ok();
    }


    // /product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRela(@RequestBody AttrRelaVo[] relaVo) {
        // 删除与分组关联的属性
        attrService.deleteBatch(relaVo);
        return  R.ok();
    }

    /**
     * 查询与分组关联的所有属性
     * */
    // /product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R listAttrRelation(@PathVariable("attrgroupId") Long attrgroupId){

        // 查询出与分组关联的所有属性
        List<AttrEntity> list = attrService.getAttrsRelation(attrgroupId);
        return R.ok().put("data", list);
    }


    /**
     * 查询所有没有与分组相关联的属性
     * /product/attrgroup/{attrgroupId}/noattr/relation
     * */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R listAttrNoRelation(@RequestParam Map<String, Object> params,@PathVariable("attrgroupId") Long attrgroupId){

       PageUtils page = attrService.getAttrsNoRelation(params,attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     * 查询三级分类所对应的分组属性
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("com.atguigu.gulimall.product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable(required = false) Long catelogId){
        // PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }




    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
   // @RequiresPermissions("com.atguigu.gulimall.product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 获取三级分类ID
        Long catelogId = attrGroup.getCatelogId();
        // 在返回分组信息时，希望找出分类id的路径
        Long[] catelogPath =  categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }



    /**
     * 保存
     */
    @RequestMapping("/save")
   // @RequiresPermissions("com.atguigu.gulimall.product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
   // @RequiresPermissions("com.atguigu.gulimall.product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
   // @RequiresPermissions("com.atguigu.gulimall.product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
