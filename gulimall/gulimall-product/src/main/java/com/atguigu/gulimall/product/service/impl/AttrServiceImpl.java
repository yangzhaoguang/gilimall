package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.vo.AttrRelaVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private AttrAttrgroupRelationServiceImpl attrAttrgroupRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /*
     * 新增规格参数并关联分组 attrAttrgroupRelationService
     * */
    @Override
    public void saveAttr(AttrVo attrVo) {
        // 1、保存属性基本信息
        AttrEntity attrEntity = new AttrEntity();
        // 拷贝对象
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.save(attrEntity);
        if (attrVo.getAttrType() == ProductConstant.ProductEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId() != null) {
            // 增加基本属性才会关联分组
            // 2、保存关联关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    /**
     - 查询属性信息，并且都是分页查询
     - 根据 key 查询
     - 根据分类ID查询
     - 查询所有
     - 查询属性分组名称
     - 根据属性信息中的 `attrId(属性ID)` 在 `pms_attr_attrgroup_relation` 表中查询出 `attr_group_id(属性分组ID)`
     - 根据 `attr_group_id(属性分组ID)` 在` pms_attr_group` 查询出 `attr_group_name(分组名)`
     - 查询所属分类名称
     - 根据属性信息中的 catelog_id(所属分类ID) 在 `pms_category`表中查询出 `name(分类名称)`
     * */
    @Override
    public PageUtils queryBaseListPage(Map<String, Object> params, Long catelogId, String type) {
        // 1、查询属性信息，并且都是分页查询
        // 判断是销售属性还是基础属性
        int code = "base".equalsIgnoreCase(type) ? ProductConstant.ProductEnum.ATTR_TYPE_BASE.getCode() :
                ProductConstant.ProductEnum.ATTR_TYPE_SALE.getCode();
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type", code);
        // 1.1 根据分类ID查询
        if (catelogId != 0) {
            // 根据所属分类查询
            wrapper.eq("catelog_id", catelogId);
        }

        // 1.2 根据 key 查询
        // attr_id/attr_name
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(queryWrapper -> {
                queryWrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        // 1.3 查询所有
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        // 属性信息集合
        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> list = records.stream().map(attrEntity -> {
            // 响应给浏览器的数据
            AttrRespVo attrRespVo = new AttrRespVo();
            // 先将属性的基本信息拷贝给vo对象
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            if (code == ProductConstant.ProductEnum.ATTR_TYPE_BASE.getCode() && attrRespVo.getAttrGroupId() != null) {
                // 只有基本属性才会关联分组
                // 2、查询属性分组名称
                // 2.1 在 `pms_attr_attrgroup_relation` 表中查询出 `attr_group_id(属性分组ID)`
                AttrAttrgroupRelationEntity attrgroupRelationEntity =
                        attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrgroupRelationEntity != null) {
                    // 2.2 根据 `attr_group_id(属性分组ID)` 在` pms_attr_group` 查询出 `attr_group_name(分组名)`
                    AttrGroupEntity attrGroupEntity =
                            attrGroupDao.selectById(attrgroupRelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            // 3、查询所属分类名称
            // 根据属性信息中的 catelog_id(所属分类ID) 在 `pms_category`表中查询出 `name(分类名称)`
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(list);
        return pageUtils;
    }

    /*
    * 回显修改的规格参数数据
         - 根据 attrId 查询 属性基本信息
        - 根据 catelogId 查询分类完整路径
        - 根据 `attrId(属性ID)` 在 `pms_attr_attrgroup_relation` 表中查询出 `attr_group_id(属性分组ID)`
        - 根据 `attr_group_id(属性分组ID)` 在` pms_attr_group` 查询出 `attr_group_name(分组名)`
    * */
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        //  根据 attrId 查询 属性基本信息
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        // 根据 catelogId 查询分类完整路径
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);

        // 根据 `attrId(属性ID)` 在 `pms_attr_attrgroup_relation` 表中查询出 `attr_group_id(属性分组ID)`
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity =
                attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
        if (attrAttrgroupRelationEntity != null) {
            attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
            // 根据 `attr_group_id(属性分组ID)` 在` pms_attr_group` 查询出 `attr_group_name(分组名)`
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
            if (attrGroupEntity != null) {
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());

            }
        }
        return attrRespVo;
    }

    /*
     * 修改规格参数
     * 1、修改规格参数
     * 2、修改关联分组ID
     * 3、判断是新增所属分组还是修改所属分组
     * */
    @Override
    public void updateAttr(AttrVo attrVo) {
        // 1、修改规格参数
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        // 只有修改基本属性，才会关联分组
        if (attrVo.getAttrType() == ProductConstant.ProductEnum.ATTR_TYPE_BASE.getCode()) {
            // 2、修改关联分组ID
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());

            UpdateWrapper<AttrAttrgroupRelationEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("attr_id", attrEntity.getAttrId());

            // 3、判断是新增所属分组还是修改所属分组
            // 从关系表中查出所属分组ID对应的数据，如果能查出来就说明是修改，查不出来就是新增
            if (attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId())) == 0) {
                // 说明是新增所属分组
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            } else {
                // 说明是修改所属分组
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, updateWrapper);
            }
        }

    }


    /**
     * 根据分组id查询出与分组关联的所有属性
     * 1、根据 attrgroupId 在关联表 pms_attr_attrgroup_relation 中查询出所对应的所有 attr_id
     *  一个分组可能对应多个属性
     * 2、根据 attr_id 在 pms_attr 表中查询出所有属性
     * */
    @Override
    public List<AttrEntity> getAttrsRelation(Long attrgroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId);
        // 查询出分组对应的所有属性
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(queryWrapper);
        // 根据查attr_id询出所有属性信息
        List<AttrEntity> allAttrEntity = list.stream()
                .map(attrAttrgroupRelationEntity -> this.getById(attrAttrgroupRelationEntity.getAttrId()))
                .collect(Collectors.toList());
        return allAttrEntity;
    }

    /**
     * 删除与分组关联的属性
     * DELETE  FROM `pms_attr_attrgroup_relation` WHERE (attr_group_id=? AND attr_id=?) OR (attr_group_id=? AND attr_id=?)
     * */
    @Override
    public void deleteBatch(AttrRelaVo[] relaVo) {
        // attrId,attrGroupId
        List<AttrRelaVo> attrRelaVos = Arrays.asList(relaVo);

        // 将 relaVo 映射成一个个的 AttrAttrgroupRelationEntity
        List<AttrAttrgroupRelationEntity> entities = attrRelaVos.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        // 删除关联关系
        attrAttrgroupRelationDao.deleteBatchRela(entities);

    }

    /**
     * 1. 在 `pms_attr_group` 表中，根据 分组ID 查询出所属的 分类ID
     * 2. 在 `pms_attr_group` 表中, 根据分类ID查询出所有的分组
     * 3. 将分组的的 分组id 映射成一个集合
     * 4. 在 `pms_attr_attrgroup_relation` 表中找出所有与 分组 相关联的属性
     * 5. 将所有相关联的 属性id 映射成一个集合
     * 6. 在 `pms_attr`表中，查询本类下的所有属性，并排除相关联的属性id集合。
     * */
    @Override
    public PageUtils getAttrsNoRelation(Map<String, Object> params, Long attrgroupId) {
        // 1. 在 `pms_attr_group` 表中，根据 分组ID 查询出所属的 分类ID
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2. 在 `pms_attr_group` 表中, 根据分类ID查询出所有的分组
        List<AttrGroupEntity> otherGroups =
                attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 3. 将分组的的 分组id 映射成一个集合
        List<Long> otherGroupsIds = otherGroups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        // 4. 在 `pms_attr_attrgroup_relation` 表中找出所有与 分组 相关联的属性
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities
                = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id",otherGroupsIds));
        // 5. 将所有分组相关联的 属性id 映射成一个集合
        List<Long> relationAttrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        // 6. 在 `pms_attr`表中，查询本类下的所有属性，并排除相关联的属性id集合。
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId);
        if (relationAttrIds != null && !relationAttrIds.isEmpty()) {
            // 有可能出现所有分组都没有关联属性的情况。
            // 并且只能关联基本属性
            queryWrapper.and((w -> {
                w.notIn("attr_id", relationAttrIds).eq("attr_type",ProductConstant.ProductEnum.ATTR_TYPE_BASE.getCode());
            }));
        }
        String key = (String) params.get("key");
        // 关键字搜索
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(queryWrapper1 -> {
                queryWrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        // 分页查询
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        // 将查询的数据封装pageUtils
        return new PageUtils(page);
    }



}