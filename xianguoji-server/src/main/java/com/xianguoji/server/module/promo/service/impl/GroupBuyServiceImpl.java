package com.xianguoji.server.module.promo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.enums.OrderStatus;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.util.OrderNoUtil;
import com.xianguoji.server.common.util.PickupCodeUtil;
import com.xianguoji.server.common.util.StockRedisHelper;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.entity.OrderStatusLog;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.order.mapper.OrderStatusLogMapper;
import com.xianguoji.server.module.promo.dto.GroupBuyLaunchDto;
import com.xianguoji.server.module.promo.dto.GroupBuyJoinDto;
import com.xianguoji.server.module.promo.entity.GroupBuyActivity;
import com.xianguoji.server.module.promo.entity.GroupBuyInstance;
import com.xianguoji.server.module.promo.entity.GroupBuyParticipant;
import com.xianguoji.server.module.promo.mapper.GroupBuyActivityMapper;
import com.xianguoji.server.module.promo.mapper.GroupBuyInstanceMapper;
import com.xianguoji.server.module.promo.mapper.GroupBuyParticipantMapper;
import com.xianguoji.server.module.promo.service.GroupBuyService;
import com.xianguoji.server.module.promo.vo.GroupBuyActivityVO;
import com.xianguoji.server.module.promo.vo.GroupBuyInstanceVO;
import com.xianguoji.server.module.user.entity.UserAddress;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import com.xianguoji.server.module.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyServiceImpl implements GroupBuyService {

    private static final String SHARE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RNG = new SecureRandom();

    private final GroupBuyActivityMapper activityMapper;
    private final GroupBuyInstanceMapper instanceMapper;
    private final GroupBuyParticipantMapper participantMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final StockRedisHelper stockRedisHelper;
    private final WsNotificationService wsNotificationService;

    @Override
    public PageVO<GroupBuyActivityVO> getGroupBuyPage(Integer page, Integer size) {
        Page<GroupBuyActivity> p = activityMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<GroupBuyActivity>()
                        .eq(GroupBuyActivity::getStatus, 1)
                        .orderByDesc(GroupBuyActivity::getCreatedAt));
        List<GroupBuyActivityVO> voList = p.getRecords().stream().map(this::toActivityVO).toList();
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long launch(Long uid, GroupBuyLaunchDto dto) {
        GroupBuyActivity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null || activity.getStatus() != 1) {
            throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团活动不存在或已结束");
        }
        if (activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团活动已结束");
        }

        ProductSku sku = skuMapper.selectById(activity.getSkuId());
        if (sku == null) throw new BizException(ResultCode.NOT_FOUND, "SKU不存在");
        boolean redisPreDeducted = false;
        if (stockRedisHelper.getStock(activity.getSkuId()) >= 0) {
            if (!stockRedisHelper.deduct(activity.getSkuId(), 1)) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
            }
            redisPreDeducted = true;
        }

        int rows = skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                .eq(ProductSku::getId, activity.getSkuId())
                .ge(ProductSku::getStock, 1)
                .setSql("stock = stock - 1, sales = sales + 1"));
        if (rows == 0) {
            if (redisPreDeducted) stockRedisHelper.rollback(activity.getSkuId(), 1);
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
        }

        GroupBuyInstance instance = new GroupBuyInstance();
        instance.setActivityId(activity.getId());
        instance.setLeaderId(uid);
        instance.setCurrentSize(1);
        instance.setTargetSize(activity.getGroupSize());
        instance.setStatus(1);
        instance.setExpireAt(LocalDateTime.now().plusHours(activity.getValidHours()));
        instance.setShareCode(generateUniqueShareCode());
        instanceMapper.insert(instance);

        GroupBuyParticipant participant = new GroupBuyParticipant();
        participant.setInstanceId(instance.getId());
        participant.setUserId(uid);
        participant.setIsLeader(1);
        participant.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        String orderNo = createGroupBuyOrder(uid, activity, instance.getId(), dto);
        participant.setOrderId(getOrderId(orderNo));
        participantMapper.updateById(participant);

        activity.setTotalJoinCount(activity.getTotalJoinCount() + 1);
        activityMapper.updateById(activity);

        // WS通知商家
        Product product = productMapper.selectById(activity.getProductId());
        String pname = product != null ? product.getName() : ("#" + activity.getProductId());
        try {
            wsNotificationService.notifyGroupBuyEvent("LAUNCH", instance.getId(), pname,
                    instance.getCurrentSize(), instance.getTargetSize());
        } catch (RuntimeException e) {
            log.warn("拼团开团通知推送失败 instanceId={}", instance.getId(), e);
        }

        return instance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long join(Long uid, Long instanceId, GroupBuyJoinDto dto) {
        GroupBuyInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null || instance.getStatus() != 1) {
            throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团不存在或已结束");
        }
        if (instance.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团已过期");
        }

        Long existing = participantMapper.selectCount(
                new LambdaQueryWrapper<GroupBuyParticipant>()
                        .eq(GroupBuyParticipant::getInstanceId, instanceId)
                        .eq(GroupBuyParticipant::getUserId, uid));
        if (existing > 0) throw new BizException(ResultCode.CONFLICT, "已参团");

        GroupBuyActivity activity = activityMapper.selectById(instance.getActivityId());

        boolean redisPreDeducted = false;
        if (stockRedisHelper.getStock(activity.getSkuId()) >= 0) {
            if (!stockRedisHelper.deduct(activity.getSkuId(), 1)) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
            }
            redisPreDeducted = true;
        }

        int stockRows = skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                .eq(ProductSku::getId, activity.getSkuId())
                .ge(ProductSku::getStock, 1)
                .setSql("stock = stock - 1, sales = sales + 1"));
        if (stockRows == 0) {
            if (redisPreDeducted) stockRedisHelper.rollback(activity.getSkuId(), 1);
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
        }

        GroupBuyParticipant participant = new GroupBuyParticipant();
        participant.setInstanceId(instanceId);
        participant.setUserId(uid);
        participant.setIsLeader(0);
        participant.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        int instanceRows = instanceMapper.update(null, new LambdaUpdateWrapper<GroupBuyInstance>()
                .eq(GroupBuyInstance::getId, instanceId)
                .eq(GroupBuyInstance::getStatus, 1)
                .lt(GroupBuyInstance::getCurrentSize, instance.getTargetSize())
                .setSql("current_size = current_size + 1"));
        if (instanceRows == 0) {
            throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团已满或已结束");
        }

        GroupBuyInstance fresh = instanceMapper.selectById(instanceId);
        boolean justSucceeded = false;
        if (fresh.getCurrentSize().equals(fresh.getTargetSize())) {
            int settleRows = instanceMapper.update(null, new LambdaUpdateWrapper<GroupBuyInstance>()
                    .eq(GroupBuyInstance::getId, instanceId)
                    .eq(GroupBuyInstance::getStatus, 1)
                    .set(GroupBuyInstance::getStatus, 2)
                    .set(GroupBuyInstance::getSuccessAt, LocalDateTime.now()));
            if (settleRows > 0) {
                justSucceeded = true;
                activity.setSuccessCount(activity.getSuccessCount() + 1);
                activityMapper.updateById(activity);
            }
        }

        String orderNo = createGroupBuyOrder(uid, activity, instanceId, dto);
        participant.setOrderId(getOrderId(orderNo));
        participantMapper.updateById(participant);

        activity.setTotalJoinCount(activity.getTotalJoinCount() + 1);
        activityMapper.updateById(activity);

        // WS推送
        Product product = productMapper.selectById(activity.getProductId());
        String pname = product != null ? product.getName() : ("#" + activity.getProductId());
        try {
            wsNotificationService.notifyGroupBuyEvent(justSucceeded ? "SUCCESS" : "JOIN",
                    instanceId, pname, fresh.getCurrentSize(), fresh.getTargetSize());
        } catch (RuntimeException e) {
            log.warn("拼团参团通知推送失败 instanceId={}", instanceId, e);
        }

        return instanceId;
    }

    @Override
    public GroupBuyInstanceVO getInstanceDetail(Long instanceId) {
        GroupBuyInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) throw new BizException(ResultCode.NOT_FOUND);
        return toInstanceVO(instance);
    }

    @Override
    public GroupBuyInstanceVO getInstanceByShareCode(String shareCode) {
        if (shareCode == null || shareCode.isBlank()) throw new BizException(ResultCode.NOT_FOUND);
        GroupBuyInstance instance = instanceMapper.selectOne(
                new LambdaQueryWrapper<GroupBuyInstance>()
                        .eq(GroupBuyInstance::getShareCode, shareCode.toUpperCase())
                        .last("LIMIT 1"));
        if (instance == null) throw new BizException(ResultCode.NOT_FOUND, "拼团不存在");
        return toInstanceVO(instance);
    }

    @Override
    public GroupBuyActivityVO getActivityByProduct(Long productId) {
        GroupBuyActivity activity = activityMapper.selectOne(
                new LambdaQueryWrapper<GroupBuyActivity>()
                        .eq(GroupBuyActivity::getProductId, productId)
                        .eq(GroupBuyActivity::getStatus, 1)
                        .ge(GroupBuyActivity::getEndTime, LocalDateTime.now())
                        .orderByDesc(GroupBuyActivity::getCreatedAt)
                        .last("LIMIT 1"));
        return activity == null ? null : toActivityVO(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanExpiredInstances() {
        List<GroupBuyInstance> expired = instanceMapper.selectList(
                new LambdaQueryWrapper<GroupBuyInstance>()
                        .eq(GroupBuyInstance::getStatus, 1)
                        .lt(GroupBuyInstance::getExpireAt, LocalDateTime.now()));

        for (GroupBuyInstance inst : expired) {
            // 仅当仍为"拼团中"时才能转为失败
            int rows = instanceMapper.update(null, new LambdaUpdateWrapper<GroupBuyInstance>()
                    .eq(GroupBuyInstance::getId, inst.getId())
                    .eq(GroupBuyInstance::getStatus, 1)
                    .set(GroupBuyInstance::getStatus, 3));
            if (rows == 0) continue;

            handleInstanceFailure(inst);
        }
    }

    /**
     * 拼团失败：取消所有未支付订单 / 已支付订单转入退款中，回补库存。
     */
    private void handleInstanceFailure(GroupBuyInstance inst) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getGroupBuyInstanceId, inst.getId()));

        for (Order order : orders) {
            // 已是终态的不处理
            Integer st = order.getStatus();
            if (st == null) continue;
            if (st == OrderStatus.CANCELLED.getCode()
                    || st == OrderStatus.REFUNDED.getCode()
                    || st == OrderStatus.COMPLETED.getCode()) {
                continue;
            }

            boolean alreadyPaid = order.getPayStatus() != null && order.getPayStatus() == 1;
            int targetStatus = alreadyPaid
                    ? OrderStatus.REFUNDING.getCode()
                    : OrderStatus.CANCELLED.getCode();
            String reason = alreadyPaid ? "拼团失败，自动发起退款" : "拼团失败，自动取消";

            int upd = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, order.getId())
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode())
                    .ne(Order::getStatus, OrderStatus.REFUNDED.getCode())
                    .ne(Order::getStatus, OrderStatus.COMPLETED.getCode())
                    .set(Order::getStatus, targetStatus)
                    .set(Order::getCancelReason, reason));
            if (upd == 0) continue;

            statusLogMapper.insert(new OrderStatusLog() {{
                setOrderId(order.getId());
                setFromStatus(st);
                setToStatus(targetStatus);
                setOperatorType(3);
                setRemark(reason);
            }});

            // 仅未支付：回补库存
            if (!alreadyPaid) {
                List<OrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
                for (OrderItem oi : items) {
                    skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                            .eq(ProductSku::getId, oi.getSkuId())
                            .setSql("stock = stock + " + oi.getQuantity()
                                    + ", sales = GREATEST(sales - " + oi.getQuantity() + ", 0)"));
                    stockRedisHelper.rollback(oi.getSkuId(), oi.getQuantity());
                }
            }
        }

        log.info("拼团实例{}失败，已处理 {} 笔关联订单", inst.getId(), orders.size());

        // WS 通知商家
        try {
            GroupBuyActivity activity = activityMapper.selectById(inst.getActivityId());
            String pname = "#" + inst.getActivityId();
            if (activity != null) {
                Product product = productMapper.selectById(activity.getProductId());
                if (product != null) pname = product.getName();
            }
            wsNotificationService.notifyGroupBuyEvent("FAIL", inst.getId(), pname,
                    inst.getCurrentSize(), inst.getTargetSize());
        } catch (RuntimeException e) {
            log.warn("拼团失败通知推送失败 instanceId={}", inst.getId(), e);
        }
    }

    @Override
    public Map<String, Object> getStats() {
        List<GroupBuyActivity> all = activityMapper.selectList(null);
        long activeCount = all.stream().filter(a -> a.getStatus() != null && a.getStatus() == 1).count();
        long totalJoin = all.stream().mapToInt(a -> a.getTotalJoinCount() == null ? 0 : a.getTotalJoinCount()).sum();
        long totalSuccess = all.stream().mapToInt(a -> a.getSuccessCount() == null ? 0 : a.getSuccessCount()).sum();

        long instanceTotal = instanceMapper.selectCount(null);
        long instanceSuccess = instanceMapper.selectCount(new LambdaQueryWrapper<GroupBuyInstance>().eq(GroupBuyInstance::getStatus, 2));
        long instanceFailed = instanceMapper.selectCount(new LambdaQueryWrapper<GroupBuyInstance>().eq(GroupBuyInstance::getStatus, 3));
        long instanceOngoing = instanceMapper.selectCount(new LambdaQueryWrapper<GroupBuyInstance>().eq(GroupBuyInstance::getStatus, 1));

        // 成团率（实例维度）
        String successRate = instanceTotal > 0
                ? String.format("%.1f", instanceSuccess * 100.0 / instanceTotal) + "%"
                : "0%";

        // 营收：成团实例的 group_price * target_size
        BigDecimal revenue = BigDecimal.ZERO;
        List<GroupBuyInstance> successInstances = instanceMapper.selectList(
                new LambdaQueryWrapper<GroupBuyInstance>().eq(GroupBuyInstance::getStatus, 2));
        for (GroupBuyInstance ins : successInstances) {
            GroupBuyActivity a = activityMapper.selectById(ins.getActivityId());
            if (a != null && a.getGroupPrice() != null && ins.getTargetSize() != null) {
                revenue = revenue.add(a.getGroupPrice().multiply(BigDecimal.valueOf(ins.getTargetSize())));
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeActivityCount", activeCount);
        stats.put("totalActivity", all.size());
        stats.put("instanceTotal", instanceTotal);
        stats.put("instanceOngoing", instanceOngoing);
        stats.put("instanceSuccess", instanceSuccess);
        stats.put("instanceFailed", instanceFailed);
        stats.put("totalJoinCount", totalJoin);
        stats.put("totalSuccessGroups", totalSuccess);
        stats.put("successRate", successRate);
        stats.put("revenue", revenue);
        return stats;
    }

    private String generateUniqueShareCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = randomShareCode();
            Long exists = instanceMapper.selectCount(
                    new LambdaQueryWrapper<GroupBuyInstance>().eq(GroupBuyInstance::getShareCode, code));
            if (exists == null || exists == 0) return code;
        }
        // 兜底：加入时间戳
        return randomShareCode() + Long.toString(System.currentTimeMillis() % 1000);
    }

    private String randomShareCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(SHARE_CODE_ALPHABET.charAt(RNG.nextInt(SHARE_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private GroupBuyInstanceVO toInstanceVO(GroupBuyInstance instance) {
        User leader = userMapper.selectById(instance.getLeaderId());
        List<GroupBuyParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<GroupBuyParticipant>()
                        .eq(GroupBuyParticipant::getInstanceId, instance.getId())
                        .orderByDesc(GroupBuyParticipant::getIsLeader));

        List<GroupBuyInstanceVO.ParticipantVO> pVOs = participants.stream().map(p -> {
            User u = userMapper.selectById(p.getUserId());
            return GroupBuyInstanceVO.ParticipantVO.builder()
                    .userId(p.getUserId())
                    .nickname(u != null ? u.getNickname() : "")
                    .avatar(u != null ? u.getAvatar() : "")
                    .isLeader(p.getIsLeader())
                    .joinedAt(p.getJoinedAt())
                    .build();
        }).toList();

        GroupBuyActivity activity = activityMapper.selectById(instance.getActivityId());
        Product product = activity != null ? productMapper.selectById(activity.getProductId()) : null;

        return GroupBuyInstanceVO.builder()
                .id(instance.getId())
                .activityId(instance.getActivityId())
                .leaderId(instance.getLeaderId())
                .leaderName(leader != null ? leader.getNickname() : "")
                .leaderAvatar(leader != null ? leader.getAvatar() : "")
                .currentSize(instance.getCurrentSize())
                .targetSize(instance.getTargetSize())
                .status(instance.getStatus())
                .expireAt(instance.getExpireAt())
                .successAt(instance.getSuccessAt())
                .shareCode(instance.getShareCode())
                .productId(activity != null ? activity.getProductId() : null)
                .skuId(activity != null ? activity.getSkuId() : null)
                .productName(product != null ? product.getName() : "")
                .mainImage(product != null ? product.getMainImage() : "")
                .groupPrice(activity != null && activity.getGroupPrice() != null
                        ? activity.getGroupPrice().toPlainString() : null)
                .groupSize(activity != null ? activity.getGroupSize() : instance.getTargetSize())
                .participants(pVOs)
                .build();
    }

    private String createGroupBuyOrder(Long uid, GroupBuyActivity activity, Long instanceId,
                                        GroupBuyLaunchDto dto) {
        return persistGroupBuyOrder(uid, activity, instanceId, dto.getDeliveryType(), dto.getDeliveryTime(),
                dto.getAddressId(), dto.getPickupPointId(), dto.getPayMethod(), dto.getUserRemark(), "拼团下单");
    }

    private String createGroupBuyOrder(Long uid, GroupBuyActivity activity, Long instanceId,
                                        GroupBuyJoinDto dto) {
        return persistGroupBuyOrder(uid, activity, instanceId, dto.getDeliveryType(), dto.getDeliveryTime(),
                dto.getAddressId(), dto.getPickupPointId(), dto.getPayMethod(), dto.getUserRemark(), "参团下单");
    }

    private String persistGroupBuyOrder(Long uid, GroupBuyActivity activity, Long instanceId,
                                        Integer deliveryType, String deliveryTime, Long addressId,
                                        Long pickupPointId, String payMethod, String userRemark,
                                        String logRemark) {
        Product product = productMapper.selectById(activity.getProductId());
        ProductSku sku = skuMapper.selectById(activity.getSkuId());

        String orderNo = OrderNoUtil.gen();
        BigDecimal payAmount = activity.getGroupPrice();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setStatus(0);
        order.setPayStatus(0);
        order.setDeliveryType(deliveryType != null ? deliveryType : 1);
        order.setDeliveryTime(deliveryTime);
        order.setGoodsAmount(activity.getGroupPrice());
        order.setPayAmount(payAmount);
        order.setGroupBuyInstanceId(instanceId);
        order.setPayMethod(payMethod);
        order.setUserRemark(userRemark);

        if (order.getDeliveryType() == 2) {
            order.setPickupPointId(pickupPointId);
            order.setPickupCode(PickupCodeUtil.gen());
        } else if (addressId != null) {
            UserAddress addr = addressMapper.selectById(addressId);
            if (addr != null) {
                order.setAddressId(addr.getId());
                order.setConsignee(addr.getConsignee());
                order.setConsigneePhone(addr.getPhone());
                order.setConsigneeAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail());
            }
        }

        orderMapper.insert(order);

        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setProductId(product.getId());
        oi.setSkuId(sku.getId());
        oi.setProductName(product.getName());
        oi.setSpecName(sku.getSpecName());
        oi.setImage(product.getMainImage());
        oi.setPrice(activity.getGroupPrice());
        oi.setOriginalPrice(sku.getOriginalPrice());
        oi.setQuantity(1);
        oi.setSubtotal(activity.getGroupPrice());
        oi.setIsReviewed(0);
        orderItemMapper.insert(oi);

        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(order.getId());
        slog.setToStatus(0);
        slog.setOperatorType(1);
        slog.setOperatorId(uid);
        slog.setRemark(logRemark);
        statusLogMapper.insert(slog);

        return orderNo;
    }

    private Long getOrderId(String orderNo) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        return o != null ? o.getId() : null;
    }

    private GroupBuyActivityVO toActivityVO(GroupBuyActivity a) {
        Product product = productMapper.selectById(a.getProductId());
        ProductSku sku = skuMapper.selectById(a.getSkuId());
        return GroupBuyActivityVO.builder()
                .id(a.getId())
                .productId(a.getProductId())
                .skuId(a.getSkuId())
                .productName(product != null ? product.getName() : "")
                .mainImage(product != null ? product.getMainImage() : "")
                .groupPrice(a.getGroupPrice())
                .originalPrice(sku != null ? sku.getOriginalPrice() : BigDecimal.ZERO)
                .groupSize(a.getGroupSize())
                .validHours(a.getValidHours())
                .endTime(a.getEndTime())
                .totalJoinCount(a.getTotalJoinCount())
                .successCount(a.getSuccessCount())
                .status(a.getStatus())
                .build();
    }
}
