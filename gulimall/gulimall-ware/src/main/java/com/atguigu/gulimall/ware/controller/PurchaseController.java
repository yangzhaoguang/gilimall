package com.atguigu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 采购信息
 *
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-15 00:03:54
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购
     *  POST /ware/purchase/done
     * */
    @PostMapping("done")
    public R done(@RequestBody PurchaseDoneVo purchaseDoneVo) {

        purchaseService.donePurchase(purchaseDoneVo);

        return R.ok();
    }

    /**
     * 领取采购单
     * /ware/purchase/merge
     */
    @PostMapping("received")
    public R received(@RequestBody List<Long> purchaseIds) {
        purchaseService.receivedPurchase(purchaseIds);

        return R.ok();
    }


    /**
     * 合并采购单
     * /ware/purchase/merge
     */
    @PostMapping("merge")
    public R merge(@RequestBody MergeVo mergeVo) {
      purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }


    /**
     * 查询未领取的采购单
     *   /ware/purchase/unreceive/list
     */
    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R unreceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPageByunreceive(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     *
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
