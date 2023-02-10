package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {


    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public List<SeckillSessionEntity> findLatest3DaysSessions() {
        // 查询最近三天的秒杀场次 2023-02-06 00:00:00 2023-02-08 23:59:59、
        List<SeckillSessionEntity> sessions = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        // 查询每个秒杀场次下的所有秒杀商品
        List<SeckillSessionEntity> list = sessions.stream().map(session -> {
            Long sessionId = session.getId();
            List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", sessionId));
            session.setRelationSkus(skuRelationEntities);
            return session;
        }).collect(Collectors.toList());

        return list;
    }


    // 秒杀场次开始时间
    private String startTime() {
        // 获取当前日期 2023-02-06
        LocalDate localDate = LocalDate.now();
        // 获取时分秒: 00:00:00
        LocalTime now = LocalTime.MIN;
        // 2023-02-06 00:00:00
        LocalDateTime dateTime = LocalDateTime.of(localDate, now);
        // 指定时间格式格式化
      return   dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // 秒杀场次结束时间
    private String endTime(){
        // 获取结束日期 2023-02-08
        LocalDate localDate = LocalDate.now().plusDays(2);
        // 获取时分秒: 11:59:59
        LocalTime now = LocalTime.MAX;
        // 2023-02-08 23:59:59
        LocalDateTime dateTime = LocalDateTime.of(localDate, now);
        // 指定时间格式格式化
        return   dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}