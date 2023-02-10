package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /*
    * 根据 品牌id 集合查询品牌信息
    * */
    @RequestMapping("/infos")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:info")
    public R getBrandsByIds(@RequestBody List<Long> brandIds) {

        List<BrandEntity> brandEntities = brandService.listByIds(brandIds);

        return R.ok().put("brands", brandEntities);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("com.atguigu.gulimall.product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * @Valid 开启校验功能
     * BindingResult 提取校验错误信息。必须紧跟着校验的JavaBean
     * 使用同一异常处理捕捉校验异常
     * @Validated spring中的开启校验注解，可配置分组
     */
    @RequestMapping("/save")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:save")
    public R save(@RequestBody @Validated(value = AddGroup.class) BrandEntity brand /*,BindingResult result*/) {
        // // 是否有错误信息
        // if (result.hasErrors()) {
        //     // 获取所有的错误
        //     List<FieldError> errors = result.getFieldErrors();
        //     HashMap<String, String> map = new HashMap<>();
        //     errors.forEach(item -> {
        //         // 错误信息
        //         String message = item.getDefaultMessage();
        //         // 错误的属性
        //         String field = item.getField();
        //         map.put(field, message);
        //     });
        //     return R.error(400,"数据提交不合法").put("data", map);
        // } else {
            brandService.save(brand);
        // }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:update")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) BrandEntity brand) {
        // brandService.updateById(brand);
        // 级联更新
        brandService.updateCascade(brand);
        return R.ok();
    }

    /**
     * 修改品牌显示状态 ShowStatus
     */
    @RequestMapping("/update/status")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:update")
    public R updateStatus(@RequestBody @Validated(value = UpdateStatus.class) BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("com.atguigu.gulimall.product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
