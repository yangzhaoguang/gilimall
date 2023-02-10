package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillInfoVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * sku检索
     * */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        //key: '华为',//检索关键字
        // catelogId: 0,
        // brandId: 0,
        // min: 0,
        // max: 0
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");


        queryWrapper.eq(!StringUtils.isEmpty(key), "sku_id", key).or().like(!StringUtils.isEmpty(key), "sku_name", key)
                .eq(!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId), "catelog_id", catelogId)
                .eq(!StringUtils.isEmpty(brandId) && !"0".equals(brandId), "brand_id", brandId)
                .ge(!StringUtils.isEmpty(min), "price", min)
                .le(!StringUtils.isEmpty(max) && new BigDecimal(max).compareTo(new BigDecimal("0")) == 1, "price", max);


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /*
     * 商品详情
     * */
    @Override
    public SkuItemVo item(Long skuId) {

        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、Sku 的基本信息：对应的表 `pms_sku_info`
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVo.setInfo(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);


        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2、Sku 的图片信息：对应的表 `pms_sku_images`
            List<SkuImagesEntity> skuImages = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setImages(skuImages);
        }, threadPoolExecutor);


        CompletableFuture<Void> skuAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、Sku 的销售属性组合
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuID(res.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、商品介绍信息：对应的表`pms_spu_info_desc`
            SpuInfoDescEntity desc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(desc);
        }, threadPoolExecutor);


        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5、商品的规格参数：一个属性分组下对应多个属性
            List<SkuItemVo.SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatelogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            // 6、查询该商品是否是秒杀商品
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo data = r.getData("data", new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(data);
            }
        }, threadPoolExecutor);


        // 等待所有异步任务结束
        try {
            CompletableFuture.allOf(baseAttrFuture, descFuture, imageFuture, skuAttrFuture,seckillFuture).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return skuItemVo;
    }


}