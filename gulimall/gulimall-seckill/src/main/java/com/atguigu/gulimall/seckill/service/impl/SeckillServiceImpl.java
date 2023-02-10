package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * Author: YZG
 * Date: 2023/2/7 11:15
 * Description: 
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    public final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    public final String SKU_STOCK_SEMAPHORE = "seckill:stocks:";



    /*
    * 秒杀
    * TODO 上架需要设计过期时间，库存锁定等》。。。
    * */
    @Override
    public String kill(String killId, String key, Integer num) {

        long s1 = System.currentTimeMillis();
        // 登录用户信息
        MemberRespVo respVo = LoginUserInterceptor.threadLocal.get();

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (!StringUtils.isEmpty(json)) {
            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

            Long startTime = skuRedisTo.getStartTime();
            Long endTime = skuRedisTo.getEndTime();
            long currentTime = System.currentTimeMillis();
            // 1、对秒杀时间进行校验
            if (currentTime >= startTime && currentTime <= endTime) {
                // 随机码
                String randomCode = skuRedisTo.getRandomCode();
                // 商品信息的key
                String redisSkuKey = skuRedisTo.getPromotionSessionId().toString() + "_" + skuRedisTo.getSkuId().toString();
                // 2、对随机码、对应关系进行校验
                if (randomCode.equals(key) && killId.equals(redisSkuKey)){
                    // 3、验证购买数量是否超出限制
                    if (skuRedisTo.getSeckillLimit() >= num){
                        // 4、判断该用户是否秒杀过 ,如果没有秒杀过就去 redis 中占个位。
                        String redisKey =  respVo.getId().toString() + "_" + skuRedisTo.getSkuId().toString();
                        // 设置过期时间: 秒杀结束就过期
                        long expir = endTime - currentTime;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), expir, TimeUnit.MILLISECONDS);
                        if (aBoolean){
                            // 如果为true说明存储成功，用户没有买过
                            // 5、使用信号量扣减库存
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);
                            try {
                                // tryAcquire 会尝试获取，不成功返回 false
                                boolean b = semaphore.tryAcquire(num);
                                if (b){
                                    // 6、抢购成功，创建订单号并返回
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo orderTo = new SeckillOrderTo();
                                    orderTo.setOrderSn(timeId);
                                    orderTo.setMemberId(respVo.getId());
                                    orderTo.setNum(num);
                                    orderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
                                    orderTo.setSkuId(skuRedisTo.getSkuId());
                                    orderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
                                    // TODO 向MQ发送消息
                                    rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                                    long s2 = System.currentTimeMillis();
                                    log.info("耗时..." + (s2 - s1));
                                    return  timeId;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        }
        return null;
    }



    /*
     * 查询某一个秒杀商品
     * */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        // 1、获取所有的key
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            // 使用正则表达式判断某个key是否包含skuId
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                // 2、将所有的key与skuId逐个匹配
                if (Pattern.matches(regx,key)) {
                    String data = hashOps.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(data, SeckillSkuRedisTo.class);

                    // 如果该商品正在秒杀，就将随机码发送给前端页面。如果没有，就无需发送
                    Long start = seckillSkuRedisTo.getStartTime();
                    Long end = seckillSkuRedisTo.getEndTime();
                    long current = System.currentTimeMillis();
                    if (!(current >= start && current <= end)) {
                        seckillSkuRedisTo.setRandomCode("");
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /*被流控之后执行的回调*/
    public List<SeckillSkuRedisTo> blockHandler(BlockException ex){
        log.error("getCurrentSeckillSkus限流....");
        return  null;
    }
    /*
     * 获取当前时间的秒杀商品
     * getCurrentSeckillSkus 资源名
     * blockHandler 被流控之后执行的回调
     * */
    @Override
    @SentinelResource(value = "getCurrentSeckillSkus",blockHandler = "blockHandler")
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 1、判断当前时间属于哪个秒杀场次
        long time = System.currentTimeMillis();
        // 获取所有的场次key
        try(Entry entry= SphU.entry("SeckillSkus")) {
            Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
            for (String key : keys) {
                // seckill:sessions:1675844252000_1675958400000 需要分割
                String[] s = key.replace(SESSION_CACHE_PREFIX, "").split("_");
                Long start = Long.parseLong(s[0]);
                Long end = Long.parseLong(s[1]);
                if (start <= time && end >= time) {
                    // 获取商品信息的value [2_11,1_10]
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    // k1:Map<k2,k3>
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    // 2、获取该场次下的所有商品信息。
                    // 由于场次信息的value与商品信息的key是一致的.可以通过场次信息的value作为商品信息的key取出value
                    List<String> skus = hashOps.multiGet(range);

                    if (skus != null && skus.size() > 0) {
                        List<SeckillSkuRedisTo> result = skus.stream().map(item -> {
                            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                            // seckillSkuRedisTo.setRandomCode(""); 秒杀已经开始，需要随机码秒杀。可以带着。
                            return seckillSkuRedisTo;
                        }).collect(Collectors.toList());
                        return result;
                    }
                    break;
                }
            }
        } catch (BlockException e) {
            log.error("资源被限流....");
        }

        return null;
    }


    /*
     * 上传秒杀商品
     * */
    @Override
    public void UploadSeckillSkuLatest3Days() {
        // 1、远程调用，查询出最近三天的秒杀场次 2023-02-06 00:00:00 2023-02-08 11:59:59
        R r = couponFeignService.findLatest3DaysSessions();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkus> sessionsWithSkus = r.getData("data", new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            if (sessionsWithSkus != null && sessionsWithSkus.size() > 0) {
                // 2、保存秒杀场次信息到 redis 中
                this.saveSessionsInfos(sessionsWithSkus);
                // 3、保存秒杀商品信息到 redis 中
                this.saveSessionsSkusInfos(sessionsWithSkus);
            } else {
                System.out.println("没有秒杀场次");
            }

        }
    }


    /*
     * 保存秒杀场次信息到redis
     * */
    private void saveSessionsInfos(List<SeckillSessionsWithSkus> sessionsWithSkus) {
        sessionsWithSkus.stream().forEach(item -> {
            // 保存的key
            long start = item.getStartTime().getTime();
            long end = item.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + start + "_" + end;
            // TODO 防止重复。保证幂等性
            if (!redisTemplate.hasKey(key)) {
                // 保存的value: sessionId_skuId
                List<String> skuIds = item.getRelationSkus().stream().map(sku -> item.getId() + "_" + sku.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    /*
     * 保存秒杀商品的详细信息
     * */
    private void saveSessionsSkusInfos(List<SeckillSessionsWithSkus> sessionsWithSkus) {
        sessionsWithSkus.stream().forEach(item -> {
            // 准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

            item.getRelationSkus().stream().forEach(relationSku -> {
                // 商品随机码
                String token = UUID.randomUUID().toString().replace("-", "");

                // TODO: 防止重复上架，保证幂等性
                if (!ops.hasKey(relationSku.getPromotionSessionId() + "_" + relationSku.getSkuId().toString())) {
                    // 将商品信息保存到redis中
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    // 1、保存秒杀商品的秒杀信息
                    BeanUtils.copyProperties(relationSku, seckillSkuRedisTo);
                    seckillSkuRedisTo.setSeckillLimit(relationSku.getSeckillLimit());
                    // 2、保存秒杀商品的详细信息
                    R r = productFeignService.info(relationSku.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuRedisTo.setSkuInfoVo(skuInfo);
                    }
                    // 3、保存秒杀的开始、结束时间
                    seckillSkuRedisTo.setStartTime(item.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(item.getEndTime().getTime());
                    // 4、保存秒杀商品的随机码
                    seckillSkuRedisTo.setRandomCode(token);
                    // 保存到 redis 中。key=sessionId_skuId
                    ops.put(relationSku.getPromotionSessionId() + "_" + relationSku.getSkuId().toString(), JSON.toJSONString(seckillSkuRedisTo));

                    // 5、设置秒杀商品的信号量 —— 秒杀的数量. 当大量请求秒杀时，不可能实时去查询数据库，这样会给数据库造成很大的压力
                    // 通过将redis中设置信号量来限制秒杀商品的数量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 将秒杀的数量作为信号量
                    semaphore.trySetPermits(relationSku.getSeckillCount().intValue());
                }
            });
        });
    }

    public static void main(String[] args) throws ParseException {
        // // 2023-02-06 16:01:00 2023-02-08 16:00:00
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Date parse = sdf.parse("2023-02-08 08:17:32");
        // System.out.println(parse.getTime());
        //
        // Date parse1 = sdf.parse("2023-02-09 16:00:00");
        // System.out.println(parse1.getTime());
        //
        // if (new Date().getTime() >=parse.getTime() && new Date().getTime() <=parse1.getTime()) {
        //     System.out.println(true);
        // }
        System.out.println(System.currentTimeMillis());

        Date date = new Date(Long.parseLong("1675844252000"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        System.out.println(format);
    }
}
