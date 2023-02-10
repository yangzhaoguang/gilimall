package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.management.ObjectName;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * Author: YZG
 * Date: 2023/1/18 16:03
 * Description: 
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /*
    * 首页渲染二级、三级分类
    * */
    @GetMapping("index/json/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatelog2Vo() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelog2Vo();
        return  map;
    }

    /*
    * 首页展示一级分类
    * */
    @GetMapping({"/","index"})
    public String indexPage(Model model) {
        // 查询所有一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevelOne();
        // 保存到请求域中
        model.addAttribute("categorys",categoryEntities);
        // 视图解析器会进行解析: classpath:/templates + index + .html
        return "index";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {

        RLock lock = redissonClient.getLock("my-lock");
        // 上锁
        lock.lock();

        // 手动指定过期时间，不会续期
        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println(Thread.currentThread().getId() + " 获取锁..");
            // 执行业务
            try {Thread.sleep(30000);} catch (InterruptedException e) {e.printStackTrace();}
        }catch (Exception e) {

        }finally {
            System.out.println(Thread.currentThread().getId() + " 释放锁..");
            // 释放锁
            lock.unlock();
        }
        return "hello";
    }

    @RequestMapping("/write")
    @ResponseBody
    public String write() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWrite-lock");
        String s = "";
        // 写锁
        readWriteLock.writeLock().lock();
        try {
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue",s);
            try {Thread.sleep(30000);} catch (InterruptedException e) {e.printStackTrace();}
        }finally {
            // 释放锁
            readWriteLock.writeLock().unlock();
        }
        return  s;
    }

    @RequestMapping("/read")
    @ResponseBody
    public String read() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWrite-lock");
        String s = "";
        // 读锁
        readWriteLock.readLock().lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        }finally {
            // 释放锁
            readWriteLock.readLock().unlock();
        }
        return  s;
    }

    /*
    * 三辆车停车
    * */
    @RequestMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        // 获取信号
        // park.acquire();

        // 尝试获取信号，成功true，否则 false
        boolean b = park.tryAcquire();
        if (b) {
            // 获取成功
        }else {
            // 获取失败
        }
        return "ok";
    }

    @RequestMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");

        // 释放信号
        park.release();

        return "ok";
    }


    /*
    * 模仿学校锁门，一共有五个班。当五个班人都走了之后，学校才会锁门
    * */
    @RequestMapping("/door")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch park = redissonClient.getCountDownLatch("lcok-door");
        // 总共有5个班
        park.trySetCount(5);
        park.await(); // 等待

        return "锁门了....";
    }


    @RequestMapping("/gogo/{id}")
    @ResponseBody
    public String gogo(@PathVariable Long id) throws InterruptedException {
        RCountDownLatch park = redissonClient.getCountDownLatch("lcok-door");
        park.countDown(); // 计数减一

        return id + " 班走了....";
    }

}
