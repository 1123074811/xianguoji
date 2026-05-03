package com.xianguoji.server.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xianguoji.server.common.enums.OrderStatus;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.entity.OrderStatusLog;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.order.mapper.OrderStatusLogMapper;
import com.xianguoji.server.module.promo.entity.GroupBuyInstance;
import com.xianguoji.server.module.promo.mapper.GroupBuyInstanceMapper;
import com.xianguoji.server.module.promo.service.GroupBuyService;
import com.xianguoji.server.module.user.entity.Footprint;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.user.mapper.FootprintMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.user.mapper.UserMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserMapper userMapper;
    private final FootprintMapper footprintMapper;
    private final GroupBuyService groupBuyService;
    private final ProductSkuMapper skuMapper;

    /**
     * 每1分钟：取消超过15分钟未支付订单
     */
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .lt(Order::getCreatedAt, threshold));

        for (Order order : orders) {
            order.setStatus(OrderStatus.CANCELLED.getCode());
            order.setCancelReason("超时未付款，系统自动取消");
            orderMapper.updateById(order);

            statusLogMapper.insert(new OrderStatusLog() {{
                setOrderId(order.getId());
                setFromStatus(OrderStatus.PENDING_PAY.getCode());
                setToStatus(OrderStatus.CANCELLED.getCode());
                setOperatorType(3);
                setRemark("超时自动取消");
            }});

            // 释放库存
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            for (OrderItem oi : items) {
                skuMapper.update(null, new LambdaUpdateWrapper<com.xianguoji.server.module.catalog.entity.ProductSku>()
                        .eq(com.xianguoji.server.module.catalog.entity.ProductSku::getId, oi.getSkuId())
                        .setSql("stock = stock + " + oi.getQuantity() + ", sales = GREATEST(sales - " + oi.getQuantity() + ", 0)"));
            }

            // 退回优惠券
            if (order.getUserCouponId() != null) {
                UserCoupon uc = new UserCoupon();
                uc.setId(order.getUserCouponId());
                uc.setStatus(0);
                uc.setOrderId(null);
                uc.setUsedAt(null);
                userCouponMapper.updateById(uc);
            }

            log.info("自动取消订单: {}", order.getOrderNo());
        }
    }

    /**
     * 每5分钟：拼团失败扫描
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void scanGroupBuyExpired() {
        groupBuyService.scanExpiredInstances();
    }

    /**
     * 每天凌晨3点：用户优惠券过期标记
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void expireUserCoupons() {
        userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getStatus, 0)
                .lt(UserCoupon::getExpireAt, LocalDateTime.now())
                .set(UserCoupon::getStatus, 2));
        log.info("已标记过期优惠券");
    }

    /**
     * 每天凌晨4点：用户标签更新（30天无下单 → silent）
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void updateUserTags() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getTag, "regular"));
        for (User u : users) {
            Long orderCount = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getUserId, u.getId())
                            .gt(Order::getCreatedAt, threshold));
            if (orderCount == 0) {
                u.setTag("silent");
                userMapper.updateById(u);
            }
        }
        log.info("用户标签更新完成");
    }

    /**
     * 每月1日凌晨：清理30天前的浏览足迹
     */
    @Scheduled(cron = "0 0 1 1 * *")
    public void cleanOldFootprints() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        footprintMapper.delete(new LambdaQueryWrapper<Footprint>().lt(Footprint::getViewedAt, threshold));
        log.info("已清理30天前的浏览足迹");
    }
}
