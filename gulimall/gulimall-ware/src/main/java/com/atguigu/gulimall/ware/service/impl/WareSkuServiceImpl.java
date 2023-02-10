package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.enume.OrderStatusEnum;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailLockedTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.*;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
@RabbitListener(queues = "stock.release.stock.queue")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        //   wareId: 123,//仓库id
        //    skuId: 123//商品id
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");

        queryWrapper.eq(!StringUtils.isEmpty(wareId), "ware_id", wareId).eq(!StringUtils.isEmpty(skuId), "sku_id", skuId);
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 设置库存
     * */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId);
        WareSkuEntity entity = this.baseMapper.selectOne(queryWrapper);
        if (entity == null) {
            // 没有对应的采购项库存，就新增
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            // TODO:设置skuName，需要远程调用
            wareSkuEntity.setSkuName("");
            this.baseMapper.insert(wareSkuEntity);
        } else {
            // 说明有与之对应的采购项库存，新增库存
            // 没有对应的采购项库存，就新增
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setStock(entity.getStock() + skuNum);
            wareSkuEntity.setId(entity.getId());
            this.baseMapper.updateById(wareSkuEntity);
        }
    }

    /*
     * 查询 sku 是否有库存
     * */
    @Override
    public HashMap<Long, Boolean> getSkusHasStock(List<Long> skuIds) {

        HashMap<Long, Boolean> map = new HashMap<>();
        for (Long skuId : skuIds) {
            // 查询库存: 当前库存 - 锁定库存
            // SELECT SUM(stock - stock_locked) FROM `wms_ware_sku` WHERE sku_id = 1
            Integer count = baseMapper.getSkusHasStock(skuId);
            map.put(skuId, count != null && count > 0);
        }

        return map;
    }

    /*
     * 锁定库存
     *  库存解锁场景：
     *  1、成功创建订单，超过指定时间未支付或者用户手动取消订单
     *  2、成功创建订单，锁定库存，但是由于接下来的业务出现异常，需要自动解锁库存，。
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 保存工作单信息
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        // 1、找到商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> hasStocks = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            stock.setSkuId(item.getSkuId());
            //  查询哪些仓库有库存。
            List<Long> wareIds = baseMapper.listWareIdHasStock(item.getSkuId());
            stock.setWareId(wareIds);
            stock.setNum(item.getCount());
            return stock;
        }).collect(Collectors.toList());


        // 2、锁定库存
        for (SkuWareHasStock hasStock : hasStocks) {
            // 标志位，表示当前商品是否被锁住
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 该商品没有库存,直接抛出异常
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 锁定库存,成功返回 1，失败返回0
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    // 当前商品锁定成功
                    skuStocked = true;
                    // TODO  3、库存锁定成功，向MQ发送消息
                    // 保存工作单详情信息
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setSkuId(skuId);
                    taskDetailEntity.setSkuNum(hasStock.getNum());
                    taskDetailEntity.setTaskId(taskEntity.getId());
                    taskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    // 向MQ发送工作单详情信息
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setTaskId(taskEntity.getId());
                    // 只封装 TaskDetailId 是不行，因为是一件商品锁定成功，发送一次消息。
                    // 如果一共有三件商品，前俩件锁定成功，第三件锁定失败。那么本地事务是会将这三件库存都会回滚。因此如果只保存id，查不到任何信息。
                    // stockLockedTo.setTaskDetailId(detailEntity.getId());

                    StockDetailLockedTo stockDetailLockedTo = new StockDetailLockedTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailLockedTo);
                    stockLockedTo.setTaskDetail(stockDetailLockedTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);

                    break;
                } else {
                    // 当前商品锁定失败,重试下一个仓库
                }
            }
            // 当前商品所有仓库都没有锁定成功
            if (!skuStocked) {
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

    /**
     * 消费者
     * @description 自动释放库存
     * @date 2023/2/4 22:51
     * @param
     * @return void
     *  1、根据工作单ID查询工作单详情信息
     *      （1）有：还需要根据订单号。查询订单
     *          - 如果没有这个订单，需要解锁：有可能订单创建成功后，库存锁定成功，接着创建订单又调用其他方法把自己搞回滚了。
     *          - 如果有订单，还需要判断订单的支付状态。如果支付成功，也无需解锁。支付失败或者取消支付进行解锁。
     *      （2）没有这个工作单：说明库存锁定失败，已经自动回滚了，无需解锁
     *
     * 应该使用手动确认机制，解锁失败重新将信息放回队列。
     */
    @Override
    public void unLock(StockLockedTo to) {
        StockDetailLockedTo taskDetail = to.getTaskDetail();
        // 查询是否有工作单详情信息
        WareOrderTaskDetailEntity orderTaskDetailEntity = wareOrderTaskDetailService.getById(taskDetail.getId());
        if (orderTaskDetailEntity != null) {
            //查出wms_ware_order_task工作单的信息
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(to.getTaskId());
            //  获取订单号查询订单状态
            R r = orderFeignService.getOrderByOrderSn(orderTaskInfo.getOrderSn());
            if (r.getCode() == 0) {
                OrderVo order = r.getData("data", new TypeReference<OrderVo>() {
                });
                if (order == null || order.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                    // 当前工作单锁定状态为已锁定，但是未解锁，才可以解锁
                    if (orderTaskDetailEntity.getLockStatus() == 1) {
                        // 没有订单或者取消订单，需要自动解锁
                        unLockStock(taskDetail.getSkuId(), taskDetail.getWareId(),taskDetail.getSkuNum(),taskDetail.getId());
                    }
                }
            }else {
                // 解锁失败
                throw new RuntimeException("解锁失败...重新入队");
            }
        }
        // 如果没有这个工作单，无需自动解锁
    }

    /**
     * @description
     * @date 2023/2/5 15:59
     * @param orderEntity
     * @return void 订单关闭，解锁库存
     */
    @Override
    public void unLock(OrderTo orderEntity) {
        // 根据订单号查询工作单
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderEntity.getOrderSn());
        // 只查询锁定的工作单详情，防止重复解锁。
        List<WareOrderTaskDetailEntity> taskDetailEntityList = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskEntity.getId())
                .eq("lock_status", 1));

        for (WareOrderTaskDetailEntity orderTaskDetailEntity : taskDetailEntityList) {
            // 解锁库存
            unLockStock(orderTaskDetailEntity.getSkuId(), orderTaskDetailEntity.getWareId(),orderTaskDetailEntity.getSkuNum(),orderTaskDetailEntity.getId());
        }
    }

    /**
     * @description
     * @date 2023/2/5 8:52
     * @param skuId
     * @param wareId
     * @param num
     * @param detailId
     * @return void 库存解锁
     */
    private void unLockStock(Long skuId, Long wareId, Integer num, Long detailId) {
        baseMapper.unLockStock(skuId, wareId, num);
        // 库存解锁后，更新库存工作单状态
        WareOrderTaskDetailEntity orderTaskDetailEntity = new WareOrderTaskDetailEntity();
        orderTaskDetailEntity.setId(detailId);
        // 变为已解锁
        orderTaskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(orderTaskDetailEntity);
    }

}

/*
 *   哪个仓库有对应的商品
 * */
@Data
class SkuWareHasStock {
    private Long skuId;
    // 锁多少件
    private Integer num;
    private List<Long> wareId;
}