package com.atguigu.gulimall.product;



import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.service.impl.CategoryServiceImpl;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * Author: YZG
 * Date: 2022/12/28 18:09
 * Description: 
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {
    // @Autowired
    // private OSS ossClient;
    // @Test
    // public void test() throws FileNotFoundException {
    //     ossClient.putObject("gulimall-bucket-2022", "0d40c24b264aa511.jpg", new FileInputStream("C:\\Java\\java_notes\\其他\\project\\谷粒商城\\资料\\docs\\pics\\0d40c24b264aa511.jpg"));
    // }

    @Autowired
    CategoryServiceImpl categoryService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }

    @Test
    public void test() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("所属分类ID的路径:{}", Arrays.toString(catelogPath));
    }

    @Test
    public void redisTest() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // 存数据
        ops.set("hello","你好" + UUID.randomUUID().toString());
        // 取出数据
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void attrGroupDaoTest() {
        List<SkuItemVo.SpuItemAttrGroupVo> list = attrGroupDao.getAttrGroupWithAttrsBySpuId(8L, 225L);
        System.out.println(list);
    }

    @Test
    public void skuSaleAttrValueDaoTest() {
        List<SkuItemVo.SkuItemSaleAttrVo> saleAttrsBySpuID = skuSaleAttrValueDao.getSaleAttrsBySpuID(8L);
        System.out.println(saleAttrsBySpuID);
    }
}
