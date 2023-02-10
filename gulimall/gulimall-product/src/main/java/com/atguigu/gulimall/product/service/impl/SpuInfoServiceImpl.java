package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.spu.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescServiceImpl spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 新增商品
     * TODO: 需要完善功能
     * */
    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1、保存商品的基本信息: gulimall_pms 数据库中的 pms_spu_info 表
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveSpuInfoBase(spuInfoEntity);

        // 2、保存商品的介绍信息: gulimall_pms 数据库中的 pms_spu_info_desc 表
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        // join方法: 拼接集合中的每一个属性，最终返回一个字符串
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3、保存商品的图片集: gulimall_pms 数据库中的 pms_spu_images 表
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveSpuInfoImages(spuInfoEntity.getId(), images);

        // 4、保存商品的基本属性: gulimall_pms 数据库中的 pms_product_attr_value 表
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();

        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setSpuId(spuInfoEntity.getId());
            valueEntity.setAttrId(attr.getAttrId());
            // 查询属性名字
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntities);

        // 5、保存商品的积分: gulimall_sms 数据库中的 sms_spu_bounds 表 【远程调用gulimall-coupon】
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r1.getCode() != 0) {
            log.error("远程保存商品积分信息失败！！");
        }

        // 6、保存当前 spu 对应的所有 sku 信息:
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(sku -> {
                // 找到sku的默认图片
                List<Images> skuImages = sku.getImages();
                String defaultImage = "";
                for (Images skuImage : skuImages) {
                    if (skuImage.getDefaultImg() == 1) {
                        defaultImage = skuImage.getImgUrl();
                    }
                }

                // （1）sku 的基本信息: gulimall_pms 数据库中的 pms_sku_info 表
                //    private String skuName;
                //     private BigDecimal price;
                //     private String skuTitle;
                //     private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatelogId(spuInfoEntity.getCatelogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfoEntity);

                // (2) sku 的图片信息: gulimall_pms 数据库中的 pms_sku_images 表
                List<SkuImagesEntity> imagesEntities = skuImages.stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                    //    图片地址可能会null，过滤以下
                }).filter(item -> !StringUtils.isEmpty(item.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                // (3) sku 的销售属性信息: gulimall_pms 数据库中的 pms_sku_sale_attr_value表 skuSaleAttrValueService
                List<Attr> skuAttrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> attrValueEntities = skuAttrs.stream().map(skuAttr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    BeanUtils.copyProperties(skuAttr, skuSaleAttrValueEntity);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(attrValueEntities);

                // (4) 保存商品的优惠、满减等信息。gulimall_sms 数据库中的 sms_sku_ladder、sms_sku_full_reduction、sms_member_price 表
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                R r = couponFeignService.saveSkuReduction(skuReductionTo);
                // 在 R 返回类中增加 getCode方法
                if (r.getCode() != 0) {
                    log.error("远程保存商品优惠信息失败！！");
                }
            });
        }
    }

    /**
     * 保存商品的基本信息
     * */
    @Override
    public void saveSpuInfoBase(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    /**
     * Spu检索查询
     * */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        //   key: '华为',//检索关键字
        //    catelogId: 6,//三级分类id
        //    brandId: 1,//品牌id
        //    status: 0,//商品状态
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String status = (String) params.get("status");

        wrapper.eq(!StringUtils.isEmpty(key), "id", key).or().like(!StringUtils.isEmpty(key), "spu_name", key)
                .eq(!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId), "catelog_id", catelogId)
                .eq(!StringUtils.isEmpty(brandId) && !"0".equals(brandId), "brand_id", brandId)
                .eq(!StringUtils.isEmpty(status), "publish_status", status);


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架功能
     *
     *  private BigDecimal skuPrice;
     *  private String skuImg;
     *  private  Boolean hasStock;
     *  private Long hotScore;
     *  private Long brandId;
     *  private Long catalogId;
     *  private String brandName;
     *  private String brandImg;
     *  private String catalogName;
     *  @Data
     *  public static class Attrs {
     *      private Long attrId;
     *      private String attrName;
     *      private String attrValue;
     * */
    @Override
    public void up(Long spuId) {

        // TODO 5、查询 sku 可检索的属性
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.listBaseAttrForSpu(spuId);
        // sku对应所有的属性id 集合
        List<Long> attrIds = attrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        // 可检索的属性
        List<AttrEntity> attrEntities =
                attrService.list(new QueryWrapper<AttrEntity>().in("attr_id", attrIds).eq("search_type", 1));
        // 可检索属性id的集合
        List<Long> searchAttrIds = attrEntities.stream().map(AttrEntity::getAttrId).collect(Collectors.toList());

        /*
        * 先过滤掉不可检索的 属性
        * 在封装数据
        * */
        List<SkuEsModel.Attrs> attrsList = attrValueEntities.stream()
                .filter(item -> searchAttrIds.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());


        List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        // TODO 2、查询是否有库存，需要远程调用 private  Boolean hasStock;
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        // key是skuid，value表示是否有库存

        Map<Long, Boolean> hasStockMap = null;
        try {
            // 远程调用可能会出现异常
            hasStockMap = wareFeignService.hasStock(skuIds);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("远程调用出现异常:{}", e);
        }

        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> esModelList = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            // TODO 1、构建SKU基本信息
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // 设置库存
            skuEsModel.setHasStock(finalHasStockMap == null || finalHasStockMap.get(sku.getSkuId()));
            // TODO 3、设置热度评分
            skuEsModel.setHotScore(0L);
            // TODO 4、 查询品牌、以及品牌信息 brandImg、brandName catalogName
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatelogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            skuEsModel.setCatalogId(sku.getCatelogId());
            // 设置可检索的属性
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());

        // TODO: 向 ES 中发送请求进行保存 gulimall-seartch
        R r = searchFeignService.productStatusUp(esModelList);
        if (r.getCode() == 0) {
            //     上架成功，还需要修改商品的发布状态
            baseMapper.updateProductPublishStatus(spuId, ProductConstant.ProductPublishStatusEnum.UP_SPU.getCode());
        } else {
            // 上架失败
            //     TODO: 可能出现的问题，重复上架，接口幂等性。。

        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        SpuInfoEntity spuInfo = this.getById(skuInfo.getSpuId());

        return spuInfo;
    }

}