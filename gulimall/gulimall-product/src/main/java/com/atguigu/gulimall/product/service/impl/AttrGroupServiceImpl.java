package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.AttrRelaVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /*
     * 根据分类ID查询所对应的分组属性
     * SELECT xx FROM pms_attr_group WHERE (catelog_id = ? AND (attr_group_id = ? OR attr_group_name LIKE ?))
     * */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        IPage<AttrGroupEntity> page;


        if (!StringUtils.isEmpty(key)) {
            // 如果搜索关键字不为空，带上关键字搜索分组
            wrapper.and(queryWrapper -> {
                queryWrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId != 0) {
            // 分类ID不等于0，根据分类Id查询分组属性
            wrapper.eq("catelog_id", catelogId);
        }

        // 如果分类ID==0，查询所有的分组属性
        page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    /**
     * 获取分类下的所有分组
     * 获取每个分组下的所有属性
     * */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsWithCatelogId(Long catelogId) {
        // 1、获取分类下的所有分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        if (attrGroupEntities != null && !attrGroupEntities.isEmpty()) {
            List<AttrGroupWithAttrsVo> attrGroupWithAttrsVoList = attrGroupEntities.stream().map(item -> {
                AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
                BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
                // 2、获取每个分组下的所有属性
                List<AttrEntity> attrs = attrService.getAttrsRelation(attrGroupWithAttrsVo.getAttrGroupId());
                attrGroupWithAttrsVo.setAttrs(attrs);

                return attrGroupWithAttrsVo;
            }).collect(Collectors.toList());

            return attrGroupWithAttrsVoList;
        }
        return null;
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catelogId) {
        List<SkuItemVo.SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catelogId);
        return vos;
    }

}