package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.common.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.CreatedOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }


    /*
     * 创建秒杀订单
     * */
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = orderTo.getSeckillPrice().multiply(BigDecimal.valueOf(orderTo.getNum()));
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //保存订单
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(orderTo.getOrderSn());
        orderItem.setRealAmount(totalPrice);

        orderItem.setSkuQuantity(orderTo.getNum());

        //保存订单项数据
        orderItemService.save(orderItem);
    }


    /*
    * 获取订单确认页的信息
    * */
    @Override
    public OrderConfirmVo orderConfirm() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // TODO 解决 Feign 异步调用丢失请求头问题: 每一个异步任务中都放一个request对象
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1、查询用户的收货地址 —— 远程调用
            List<MemberAddressVo> address = memberFeignService.getMemberAddresses(memberRespVo.getId());
            orderConfirmVo.setAddress(address);
        }, threadPoolExecutor);


        CompletableFuture<Void> orderItemsFuture = CompletableFuture.runAsync(() -> {
            // TODO 解决 Feign 异步调用丢失请求头问题: 每一个异步任务中都放一个request对象
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2、查询订单的购物项 —— 远程调用
            // Feign在远程调用之前，会构造请求。
            List<OrderItemVo> items = cartFeignService.currentUserCartItems();
            orderConfirmVo.setItems(items);
        }, threadPoolExecutor).thenRunAsync(() -> {
            // 3、查询库存状态
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            HashMap<Long, Boolean> map = wareFeignService.hasStock(skuIds);
            orderConfirmVo.setStocks(map);
        }, threadPoolExecutor);

        // 4、积分信息
        orderConfirmVo.setIntegration(memberRespVo.getIntegration());

        CompletableFuture.allOf(addressFuture, orderItemsFuture).get();
        // 总价格、应付价格自动封装...

        // TODO 订单防重复令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token);


        return orderConfirmVo;
    }


// b 和 a 共享一个事务。c是一个新事物
// SpringBoot 中的坑：如果在同一个类中编写方法，进行内部调用。会导致事务设置失败。如果在 a中直接掉用 b、c 那么b、c的事务设置不会生效。
// 解决：使用 aop 中的 ASpectJ 动态代理
/*    @Transactional(timeout = 20)
    public void a() {
        // 使用动态代理：可直接转换为任意对象
        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        orderService.b();
        orderService.c();
    }

    // 由于 b 和 a 共享一个事务，因此 b 事务中的所有配置都没有用。
    @Transactional(propagation = Propagation.REQUIRED,timeout = 2)
    public void b() {
        System.out.println("b");
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void c() {
        System.out.println("c");
    }*/


    /**
     * @description 提交订单
     * @date 2023/2/4 15:00
     * @param vo
     * @return com.atguigu.gulimall.order.vo.OrderSubmitResponseVo
     * @Transactional(rollbackFor = NoStockException.class) ：本地事务，只能保证本地的事务能够回滚。分布式下不能保证
     * @GlobalTransactional seata 分布式全局事务。
     * 在高并发下 seata 的AT模式性能并不高。适用一些并发量并不高的场景
     * 使用 可靠消息+最终一致性方案。当锁定库存失败后，向MQ发送一条消息，通知它去解锁库存。这样相较于 seata 来说性能高很多。
     *
     */
    @Transactional(rollbackFor = NoStockException.class)
    // @GlobalTransactional
    @Override
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo vo) throws NoStockException {
        OrderSubmitResponseVo responseVo = new OrderSubmitResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        // 1、验证令牌【必须保证获取令牌、删除令牌的原子性】
        String orderToken = vo.getOrderToken();
        // LUA 脚本: 0代表删除失败，1 代表删除成功
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 执行 LUA 脚本
        Long execute = redisTemplate.execute(
                new DefaultRedisScript<Long>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);
        if (execute != null && execute == 0) {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            // 令牌验证成功: 创建订单、验证价格、锁定库存....
            //  TODO 1、创建订单
            CreatedOrderTo orderTo = createOrder(vo);
            // TODO 2、验证价格
            BigDecimal payPrice = vo.getPayPrice();
            BigDecimal payAmount = orderTo.getOrder().getPayAmount();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // TODO 3、验价成功，保存订单
                saveOrder(orderTo);
                // TODO 4、锁定库存，库存不足抛出异常，并回滚事务
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                List<OrderItemVo> locks = createOrderItems(orderTo.getOrder().getOrderSn()).stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());

                wareSkuLockVo.setOrderSn(orderTo.getOrder().getOrderSn());
                wareSkuLockVo.setLocks(locks);
                // TODO 远程调用锁定库存
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // 锁定成功
                    responseVo.setOrder(orderTo.getOrder());
                    // TODO 5、订单回滚，库存不回滚。
                    // int i = 10/ 0;
                    // TODO 创建订单成功，向RabbitMQ发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderTo.getOrder());
                    return responseVo;
                } else {
                    // 锁定失败.抛出异常
                    responseVo.setCode(3);
                    throw new NoStockException();
                }
            } else {
                // 验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
        // 这种方式不能保证原子性。应该使用 LUA脚本或者Redisson分布式锁
        /* String redisToken = (String) redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        if (orderToken != null && orderToken.equals(redisToken)) {
            // 令牌验证通过
        }else {
            // 令牌验证失败
        }*/
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /*
     * 关闭订单
     * */
    @Override
    public void closeOrder(OrderEntity order) {
        Long orderId = order.getId();
        // 为了保险起见，在重新查一遍订单信息
        OrderEntity orderEntity = this.getById(order);
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            // 订单为代付款时，允许取消订单
            OrderEntity entity = new OrderEntity();
            entity.setId(orderEntity.getId());
            // 设置订单状态为取消
            entity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(entity);
            // TODO 关闭订单后，再次向MQ发送一条消息
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            try {
                // TODO 为了保证消息一定能够送达，采取一定的方案：比如往数据库中保存消息的状态信息，重发
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (AmqpException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @description
     * @date 2023/2/5 20:22
     * @param orderSn
     * @return com.atguigu.gulimall.order.vo.PayVo 支付订单
     */
    @Override
    public PayVo getOrderPay(String orderSn) {

        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);// 订单号

        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        payVo.setSubject(orderItemEntities.get(0).getSpuName());// 订单标题
        // 指定后面2位小数
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2).toString());
        payVo.setBody(orderItemEntities.get(0).getSpuName()); // 备注

        return payVo;
    }

    /*
    * 查询登录用户的所有订单
    * */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberRespVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> records = page.getRecords();

        // 获取订单中的所有订单项
        List<OrderEntity> newRecords = records.stream().map(order -> {
            List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(list);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(newRecords);

        return new PageUtils(page);
    }


    /*
     * 保存订单、订单项
     * */
    private void saveOrder(CreatedOrderTo orderTo) {
        OrderEntity order = orderTo.getOrder();
        order.setCreateTime(new Date());
        // 保存订单
        this.save(order);

        // 保存订单项
        orderItemService.saveBatch(orderTo.getItems());

    }

    /*
     * 创建 CreatedOrderTo
     * */
    private CreatedOrderTo createOrder(OrderSubmitVo vo) {
        CreatedOrderTo orderTo = new CreatedOrderTo();

        // 1、构建订单
        OrderEntity orderEntity = createOrderEntity(vo);
        // 创建订单号: MyBatis-Plus 中自动生成
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);

        // 2、构建订单项
        List<OrderItemEntity> items = createOrderItems(orderSn);

        // 3、计算订单价格
        assert items != null;
        computePrice(items, orderEntity);

        orderTo.setOrder(orderEntity);
        orderTo.setItems(items);
        return orderTo;
    }

    /*
     * 计算订单的价格
     * */
    private void computePrice(List<OrderItemEntity> items, OrderEntity orderEntity) {

        // 总价
        BigDecimal total = new BigDecimal("0.0");
        // 优惠券优惠分解金额
        BigDecimal coupon = new BigDecimal("0.0");
        // 积分优惠分解金额
        BigDecimal integration = new BigDecimal("0.0");
        // 商品促销分解金额
        BigDecimal promotion = new BigDecimal("0.0");

        // 积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        // 循环叠加每一个订单项的优惠价格、订单总价格、积分、成长信息
        for (OrderItemEntity orderItem : items) {

            // 优惠价格
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            integration = integration.add(orderItem.getIntegrationAmount());
            // 订单总价格
            total = total.add(orderItem.getRealAmount());
            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }

        // 设置订单的总价格、优惠价格、积分信息
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);
    }

    /*
     * 构建 OrderEntity
     * */
    @NotNull
    private OrderEntity createOrderEntity(OrderSubmitVo vo) {
        // 一、构建订单数据
        OrderEntity orderEntity = new OrderEntity();

        // 1、订单收货信息、运费
        R r = wareFeignService.fare(vo.getAddrId());
        if (r.getCode() == 0) {
            FareVo fareVo = r.getData("data", new TypeReference<FareVo>() {
            });
            orderEntity.setFreightAmount(fareVo.getFare());
            orderEntity.setMemberId(fareVo.getAddress().getMemberId());
            orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
            orderEntity.setReceiverCity(fareVo.getAddress().getCity());
            orderEntity.setReceiverName(fareVo.getAddress().getName());
            orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
            orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
            orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
            orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        }
        // 2、设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setConfirmStatus(0);
        return orderEntity;
    }

    /*
     *  createdOrderItems:构建所有的订单项
     * */
    private List<OrderItemEntity> createOrderItems(String orderSn) {
        // 购物车中的所有购物项
        List<OrderItemVo> orderItemVos = cartFeignService.currentUserCartItems();
        if (orderItemVos != null && orderItemVos.size() > 0) {
            // 将 orderItemVo ——> orderItemEntity
            List<OrderItemEntity> items = orderItemVos.stream().map(orderItemVo -> {
                // 构建订单项
                OrderItemEntity orderItemEntity = createOrderItem(orderItemVo);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return items;
        }
        return null;
    }

    /*
     * orderItemVo 构建每个订单项
     * */
    private OrderItemEntity createOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 1、订单信息 √
        // 2、spu信息 —— 远程调用
        R r = productFeignService.getSpuInfoBySkuId(orderItemVo.getSkuId());
        if (r.getCode() == 0) {
            SpuInfoVo spuInfoVo = r.getData("data", new TypeReference<SpuInfoVo>() {
            });
            orderItemEntity.setSpuId(spuInfoVo.getId());
            orderItemEntity.setSpuName(spuInfoVo.getSpuName());
            orderItemEntity.setCategoryId(spuInfoVo.getCatelogId());
            orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        }
        // 3、sku信息
        orderItemEntity.setSkuId(orderItemVo.getSkuId());
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getDefaultImage());
        // collectionToDelimitedString spring提供的，可以将list集合按照指定字符拼接成string字符串
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttr(), ";"));
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        // 4、优惠信息 —— 省略
        // 5、积分信息
        orderItemEntity.setGiftIntegration(orderItemVo.getPrice().intValue());
        orderItemEntity.setGiftGrowth(orderItemVo.getPrice().intValue());
        // 6、每个订单项的价格 = sku价格 * sku数量
        // 商品促销分解金额
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        // 优惠券优惠分解金额
        orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
        // 积分优惠分解金额
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));

        // 订单项最终的价格
        BigDecimal initPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal finalPrice = initPrice
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());

        orderItemEntity.setRealAmount(finalPrice);


        return orderItemEntity;
    }

    /*
    * 支付成功异步回调通知
    * */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        // 1、保存交易流水
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(vo.getOut_trade_no());
        paymentInfo.setAlipayTradeNo(vo.getTrade_no());
        paymentInfo.setTotalAmount(new BigDecimal(vo.getBuyer_pay_amount()));
        paymentInfo.setSubject(vo.getBody());
        paymentInfo.setPaymentStatus(vo.getTrade_status());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(vo.getNotify_time());
        // 添加到数据库中
        this.paymentInfoService.save(paymentInfo);

        // 修改订单状态
        // 获取当前状态
        String tradeStatus = vo.getTrade_status();

        if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
            //支付成功状态
            String orderSn = vo.getOut_trade_no(); //获取订单号
            this.updateOrderStatus(orderSn,OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }


    /**
     * 修改订单状态
     * @param orderSn
     * @param status 订单状态
     */
    private void updateOrderStatus(String orderSn, Integer status) {

        this.baseMapper.updateOrderStatus(orderSn,status);
    }

}