package com.atguigu.gulimall.ware.service.impl;


import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneItemVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询未领取的采购单
     * */
    @Override
    public PageUtils queryPageByunreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    /**
     * 合并采购单
     * */
    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        // 新建采购单
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            // 获取新的采购单id
            purchaseId = purchaseEntity.getId();
        }

        // 确认采购单状态，只有0或者1才能合并
        PurchaseEntity purchase = this.getById(purchaseId);
        Integer status = purchase.getStatus();

        if (status == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                status == WareConstant.PurchaseStatusEnum.ASSIGNEE.getCode()) {
            // 合并采购单
            List<Long> items = mergeVo.getItems();
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                // 1、设置采购需求的采购单
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                // 2、设置采购需求的状态为 已分配
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNEE.getCode());
                purchaseDetailEntity.setId(item);

                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            // 批量修改
            purchaseDetailService.updateBatchById(collect);

            // 同时更新采购单的修改时间
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setId(purchaseId);
            this.updateById(purchaseEntity);
        }

        // switch (status) {
        //     case 2:
        //        return R.error("采购单已被领取");
        //     case 3:
        //         return R.error("采购单已完成采购");
        //     default:
        //         return R.error("采购单有异常");
        // }
    }

    /**
     * 领取采购单
     * */
    @Override
    @Transactional
    public void receivedPurchase(List<Long> purchaseIds) {
        // - 判断采购单状态
        // (1) 根据 purchaseId 查询出所有的采购单
        // (2) 过滤掉采购单状态不为 0 或者 1 ，剩下的就是可领取的采购单
        List<PurchaseEntity> unReceivePurchases = purchaseIds.stream()
                .map(this::getById)
                .filter(entity -> entity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                        entity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNEE.getCode())
                .collect(Collectors.toList());

        // - 修改采购单状态为已领取
        List<PurchaseEntity> purchaseEntityList = unReceivePurchases.stream().map(item -> {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(item.getId());
            // 设置采购单状态为已领取
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            return purchaseEntity;
        }).collect(Collectors.toList());
        this.updateBatchById(purchaseEntityList);


        // - 修改采购需求的状态为正在采购
        unReceivePurchases.forEach(item -> {
            // 查询出采购单中所有的采购需求
            List<PurchaseDetailEntity> purchaseDetailEntityList =
                    purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", item.getId()));

            // 修改每一个采购需求中的status
            List<PurchaseDetailEntity> collect = purchaseDetailEntityList.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                BeanUtils.copyProperties(entity, purchaseDetailEntity);
                // 修改采购需求的状态为正在采购
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect);
        });
    }

    /**
     * 完成采购
     * */
    @Override
    @Transactional
    public void donePurchase(PurchaseDoneVo purchaseDoneVo) {
        // 1. 设置采购项的状态
        //    1. 采购项的状态根据请求参数中的 status 决定的
        //
        // 2. 设置采购单的状态，采购单的状态是根据采购项的状态决定的
        //    1. 如果所有的采购项都采购成功，那么采购单的状态是 FINISHED
        //    2. 如果有一个采购项没有采购成功，那么采购单的状态是 HASERROR
        // 3. 设置库存
        //    1. 将采购成功的采购项增加到库存当中
        //       1. 如果库存中没有这个采购项，就新建一个采购项的库存
        //       2. 如果库存中有这个采购项，就修改库存中采购项的数量
        Long purchaseId = purchaseDoneVo.getId();

        // 采购项集合
        List<PurchaseDoneItemVo> items = purchaseDoneVo.getItems();
        // 保存采购项
        ArrayList<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();

        boolean isError = true;

        for (PurchaseDoneItemVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item.getItemId());
            // 设置采购项状态
            purchaseDetailEntity.setStatus(item.getStatus());

            purchaseDetailEntities.add(purchaseDetailEntity);
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                // 如果有采购项失败
                isError = false;
            }else{
                // 3、采购成功的采购项，设置库存
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum());
            }
        }

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(isError ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        // 2、更新采购单状态
        this.updateById(purchaseEntity);
        // 1、批量更新采购项的状态
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
    }

}