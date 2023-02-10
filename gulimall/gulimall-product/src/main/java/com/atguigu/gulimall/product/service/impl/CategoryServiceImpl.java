package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationServiceImpl categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    // 查询所有分类，封装成树形结构
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查询出所有分类
        List<CategoryEntity> all = baseMapper.selectList(null);

        List<CategoryEntity> level1 = all.stream()
                .filter(categoryEntity ->  // 2、先找出所有的一级分类
                        categoryEntity.getParentCid() == 0
                ).map(menu -> { // 3、找出每个一级分类下的所有子分类
                    menu.setChildren(getsetChildrens(menu, all));
                    return menu;
                }).sorted((menu1, menu2) -> {   // 4、根据 Sort 字段排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());

        return level1;
    }

    /**
     * 删除菜单
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO: 判断别的地方是否存在引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * @description 找到所属分类id的路径 [父级分类id，儿子分类id，孙子分类id]
     * @date 2022/12/31 16:22
     * @param catelogId
     * @return java.lang.Long[]
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {

        ArrayList<Long> catelogPath = new ArrayList<Long>();
        // 找到所属分类id的路径，是一个递归操作
        findParentPath(catelogId, catelogPath);

        // 最后封装好的集合是: {孙子分类id,儿子分类id,父级分类id}
        // 将它反转一下: {父级分类id，儿子分类id，孙子分类id}
        Collections.reverse(catelogPath);

        // 由于将集合转换为数组，是一个Object类型的数组，Object类型数组转换为Long类型会报：CastClassException
        return catelogPath.toArray(new Long[catelogPath.size()]);

    }

    /*
     * 更新分类表的同时更新其他关联表
     *  批量删除多个缓存数据有俩种方法：
     * 1、使用  @Caching 注解，可组合多个缓存动作
     * 2、@CacheEvict(value = {"category"},allEntries = true)  allEntries 表示删除category分区下的所有key
     * */
    // @Caching(evict = {
    //         @CacheEvict(value = {"category"},key = "'getLevelOne'"),
    //         @CacheEvict(value = {"category"},key = "'getCatelogJSON'")
    // })
    @CacheEvict(value = {"category"},allEntries = true)
    @Transactional // 事务注解
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        // 更新 关联表的 分类名
        if (!StringUtils.isEmpty(category.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setCatelogName(category.getName());
            categoryBrandRelationService.update(categoryBrandRelationEntity,
                    new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", category.getCatId()));
        }
        // TODO 更新其他表的数据
    }

    /*
     * 首页获取一级分类
     * @Cacheable 方法返回的结果会存入缓存中：
     *      如果缓存中有则无需调用方法，直接返回缓存中的值
     *      如果缓存中没有将方法返回的结果存到缓存中
     *  value : 可以设置缓存的分区名，建议使用业务名区分。
     *  key : 缓存的key，默认使用SpEL 表达式，想要使用字符串必须使用 ''
     * */
    @Cacheable(value = {"category"},key = "'getLevelOne'")
    @Override
    public List<CategoryEntity> getLevelOne() {
        System.out.println("进入到 getLevelOne 方法...");
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

    }


    /*
     * 首页获取二级、三级分类——使用SpringCache缓存注解开发
     * */
    @Override
    @Cacheable(value = {"category"},key = "'getCatelogJSON'")
    public Map<String, List<Catelog2Vo>> getCatelog2Vo() {

        System.out.println(Thread.currentThread().getName() + " 查询了数据库....");
        // 查询出所有的分类
        List<CategoryEntity> allCategorys = this.list();
        // 1、查询出所有的一级分类
        List<CategoryEntity> levelOneList = getCategoryListByParentCid(allCategorys, 0L);


        // 将结果封装成一个 map
        Map<String, List<Catelog2Vo>> map = levelOneList.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2、查询出所有的二级分类
            List<CategoryEntity> levelTwoList = getCategoryListByParentCid(allCategorys, v.getCatId());

            List<Catelog2Vo> catelog2VoList = null;
            if (levelTwoList != null) {
                // 3、封装二级分类
                catelog2VoList = levelTwoList.stream().map(levelTwo -> {

                    Catelog2Vo catelog2Vo =
                            new Catelog2Vo(levelTwo.getParentCid().toString(), null, levelTwo.getCatId().toString(), levelTwo.getName());
                    // 4、查询出所有的三级分类
                    List<CategoryEntity> levelThreeList = getCategoryListByParentCid(allCategorys, levelTwo.getCatId());

                    if (levelThreeList != null) {
                        // 5、封装三级分类
                        List<Catelog2Vo.Catelog3Vo> catalog3List = levelThreeList.stream().map(levelThree -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo =
                                    new Catelog2Vo.Catelog3Vo(levelTwo.getCatId().toString(), levelThree.getCatId().toString(), levelThree.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());

                        catelog2Vo.setCatalog3List(catalog3List);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2VoList;
        }));
        return map;
    }

    /*
     * 首页获取二级、三级分类——使用Redis缓存代码
     * 防止出现缓存击穿、穿透、雪崩“
     *  1、缓存空结果
     *  2、设置过期时间
     *  3、加锁
     * */
    /*public Map<String, List<Catelog2Vo>> getCatelog2VoWithRedis() {
        // 尝试获取数据
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");

        if (StringUtils.isEmpty(catalogJSON)) {
            // redis没有数据，查询数据库
            // System.out.println("缓存未命中....即将查询数据库....");
            Map<String, List<Catelog2Vo>> catelog2VoByDb = getCatelog2VoFromDbWithRedis();

            // 存入 redis，将对象转换为 JSON,并设置过期时间
            // 以后存储 redis ，都存储JSON数据，因为JSON是跨平台，跨语言的。
            // stringRedisTemplate.opsForValue()
            //         .set("catalogJSON", JSON.toJSONString(catelog2VoByDb),new Random().nextInt(3), TimeUnit.DAYS);
            return catelog2VoByDb;
        }
        // System.out.println("缓存命中....直接返回结果....");
        // 将结果转换为对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return result;
    }*/

    /*
     *
     *  获取二、三级分类——本地锁
     * */
    /*public Map<String, List<Catelog2Vo>> getCatelog2VoFromDbWithSynchronized() {

        synchronized (this) {
            if (!StringUtils.isEmpty(stringRedisTemplate.opsForValue().get("catalogJSON"))) {
                String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return result;
            }
            System.out.println(Thread.currentThread().getName() + " 查询数据库...");

            return getDataFromDB();
        }

    }*/


    /*
     *
     *  获取二、三级分类——基于Redis的分布式锁
     * */
    /*public Map<String, List<Catelog2Vo>> getCatelog2VoFromDbWithRedis() {

        String uuid = UUID.randomUUID().toString();
        // 设置锁
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {

            // 设置锁的过期时间，不推荐！ 不是原子操作
            // stringRedisTemplate.expire("lock",300,TimeUnit.SECONDS);
            // 占锁成功,查询数据库
            Map<String, List<Catelog2Vo>> dataFromDB = null;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                // 释放锁
                // stringRedisTemplate.delete("lock");
                // 使用 LUA 脚本释放锁
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<Long>(script, Long.class);
                // 执行脚本
                stringRedisTemplate.execute(defaultRedisScript, Arrays.asList("lock"), uuid);
            }
            return dataFromDB;
        } else {
            // 自旋的方式，尝试获取锁
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelog2VoFromDbWithRedis();
        }
    }*/

    /*
     *
     *  获取二、三级分类——基于Redisson的分布式锁
     * */
   /* public Map<String, List<Catelog2Vo>> getCatelog2VoFromDbWithRedisson() {

        RLock lock = redisson.getLock("catalogJSON-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDB = null;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }*/

    /*
     * 获取二、三级分类——真正查询数据库的操作
     * */
    /*private Map<String, List<Catelog2Vo>> getDataFromDB() {
        // 先从redis尝试获取
        if (!StringUtils.isEmpty(stringRedisTemplate.opsForValue().get("catalogJSON"))) {
            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println(Thread.currentThread().getName() + " 查询了数据库....");
        // 查询出所有的分类
        List<CategoryEntity> allCategorys = this.list();
        // 1、查询出所有的一级分类
        List<CategoryEntity> levelOneList = getCategoryListByParentCid(allCategorys, 0L);


        // 将结果封装成一个 map
        Map<String, List<Catelog2Vo>> map = levelOneList.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2、查询出所有的二级分类
            List<CategoryEntity> levelTwoList = getCategoryListByParentCid(allCategorys, v.getCatId());

            List<Catelog2Vo> catelog2VoList = null;
            if (levelTwoList != null) {
                // 3、封装二级分类
                catelog2VoList = levelTwoList.stream().map(levelTwo -> {

                    Catelog2Vo catelog2Vo =
                            new Catelog2Vo(levelTwo.getParentCid().toString(), null, levelTwo.getCatId().toString(), levelTwo.getName());
                    // 4、查询出所有的三级分类
                    List<CategoryEntity> levelThreeList = getCategoryListByParentCid(allCategorys, levelTwo.getCatId());

                    if (levelThreeList != null) {
                        // 5、封装三级分类
                        List<Catelog2Vo.Catelog3Vo> catalog3List = levelThreeList.stream().map(levelThree -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo =
                                    new Catelog2Vo.Catelog3Vo(levelTwo.getCatId().toString(), levelThree.getCatId().toString(), levelThree.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());

                        catelog2Vo.setCatalog3List(catalog3List);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2VoList;
        }));

        // 存入 redis
        stringRedisTemplate.opsForValue()
                .set("catalogJSON", JSON.toJSONString(map), 1, TimeUnit.DAYS);
        return map;
    }*/

    /*
     * 获取二、三级分类——根据父分类id获取指定的分类集合
     * */
    private List<CategoryEntity> getCategoryListByParentCid(List<CategoryEntity> allCategorys, Long parentCid) {
        return allCategorys.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
    }


    /*
     * 找到所属分类id的路径
     * */
    private void findParentPath(Long catelogId, List<Long> catelogPath) {
        catelogPath.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            // 如果所属分类的父id不等于0，继续递归查找
            findParentPath(categoryEntity.getParentCid(), catelogPath);
        }


    }

    /**
     * @description
     * @date 2022/12/25 19:50
     * @param entity 父级分类
     * @param all 所有分类的集合
     * @return java.util.List<com.atguigu.gulimall.product.entity.CategoryEntity>
     */
    private List<CategoryEntity> getsetChildrens(CategoryEntity entity, List<CategoryEntity> all) {

        List<CategoryEntity> treeList = all.stream()
                .filter(categoryEntity -> { // 1、找出集合中分类对应的所有子分类
                    return categoryEntity.getParentCid().longValue() == entity.getCatId().longValue();
                }).map(categoryEntity -> { // 2、递归查找所有的子分类
                    categoryEntity.setChildren(getsetChildrens(categoryEntity, all));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());

        return treeList;
    }
}
