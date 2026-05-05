package com.xianguoji.server.module.promo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.util.OrderNoUtil;
import com.xianguoji.server.common.util.PickupCodeUtil;
import com.xianguoji.server.common.util.StockRedisHelper;
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
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyServiceImpl implements GroupBuyService {

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

        // Redis 预扣库存（拼团场景）
        ProductSku sku = skuMapper.selectById(activity.getSkuId());
        if (sku == null) throw new BizException(ResultCode.NOT_FOUND, "SKU不存在");
        boolean redisPreDeducted = false;
        if (stockRedisHelper.getStock(activity.getSkuId()) >= 0) {
            // Redis key 存在，走预扣路径
            if (!stockRedisHelper.deduct(activity.getSkuId(), 1)) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
            }
            redisPreDeducted = true;
        }

        // DB 乐观锁兜底
        int rows = skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                .eq(ProductSku::getId, activity.getSkuId())
                .ge(ProductSku::getStock, 1)
                .setSql("stock = stock - 1, sales = sales + 1"));
        if (rows == 0) {
            if (redisPreDeducted) stockRedisHelper.rollback(activity.getSkuId(), 1);
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
        }

        // 创建拼团实例
        GroupBuyInstance instance = new GroupBuyInstance();
        instance.setActivityId(activity.getId());
        instance.setLeaderId(uid);
        instance.setCurrentSize(1);
        instance.setTargetSize(activity.getGroupSize());
        instance.setStatus(1);
        instance.setExpireAt(LocalDateTime.now().plusHours(activity.getValidHours()));
        instanceMapper.insert(instance);

        // 团长参与记录
        GroupBuyParticipant participant = new GroupBuyParticipant();
        participant.setInstanceId(instance.getId());
        participant.setUserId(uid);
        participant.setIsLeader(1);
        participant.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        // 创建订单
        String orderNo = createGroupBuyOrder(uid, activity, instance.getId(), dto);
        participant.setOrderId(getOrderId(orderNo));
        participantMapper.updateById(participant);

        // 更新活动参团人次
        activity.setTotalJoinCount(activity.getTotalJoinCount() + 1);
        activityMapper.updateById(activity);

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

        // 检查是否已参团
        Long existing = participantMapper.selectCount(
                new LambdaQueryWrapper<GroupBuyParticipant>()
                        .eq(GroupBuyParticipant::getInstanceId, instanceId)
                        .eq(GroupBuyParticipant::getUserId, uid));
        if (existing > 0) throw new BizException(ResultCode.CONFLICT, "已参团");

        GroupBuyActivity activity = activityMapper.selectById(instance.getActivityId());

        // Redis 预扣库存（拼团场景）
        boolean redisPreDeducted = false;
        if (stockRedisHelper.getStock(activity.getSkuId()) >= 0) {
            if (!stockRedisHelper.deduct(activity.getSkuId(), 1)) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
            }
            redisPreDeducted = true;
        }

        // DB 乐观锁兜底
        int stockRows = skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                .eq(ProductSku::getId, activity.getSkuId())
                .ge(ProductSku::getStock, 1)
                .setSql("stock = stock - 1, sales = sales + 1"));
        if (stockRows == 0) {
            if (redisPreDeducted) stockRedisHelper.rollback(activity.getSkuId(), 1);
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
        }

        // 参团记录
        GroupBuyParticipant participant = new GroupBuyParticipant();
        participant.setInstanceId(instanceId);
        participant.setUserId(uid);
        participant.setIsLeader(0);
        participant.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        // 原子自增 currentSize，防止并发超员
        int instanceRows = instanceMapper.update(null, new LambdaUpdateWrapper<GroupBuyInstance>()
                .eq(GroupBuyInstance::getId, instanceId)
                .eq(GroupBuyInstance::getStatus, 1)
                .lt(GroupBuyInstance::getCurrentSize, instance.getTargetSize())
                .setSql("current_size = current_size + 1"));
        if (instanceRows == 0) {
            throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团已满或已结束");
        }

        // 重新查 currentSize 判断是否成团
        GroupBuyInstance fresh = instanceMapper.selectById(instanceId);
        if (fresh.getCurrentSize().equals(fresh.getTargetSize())) {
            instanceMapper.update(null, new LambdaUpdateWrapper<GroupBuyInstance>()
                    .eq(GroupBuyInstance::getId, instanceId)
                    .eq(GroupBuyInstance::getStatus, 1)
                    .set(GroupBuyInstance::getStatus, 2)
                    .set(GroupBuyInstance::getSuccessAt, LocalDateTime.now()));
            activity.setSuccessCount(activity.getSuccessCount() + 1);
            activityMapper.updateById(activity);
        }

        // 创建订单
        String orderNo = createGroupBuyOrder(uid, activity, instanceId, dto);
        participant.setOrderId(getOrderId(orderNo));
        participantMapper.updateById(participant);

        activity.setTotalJoinCount(activity.getTotalJoinCount() + 1);
        activityMapper.updateById(activity);

        return instanceId;
    }

    @Override
    public GroupBuyInstanceVO getInstanceDetail(Long instanceId) {
        GroupBuyInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) throw new BizException(ResultCode.NOT_FOUND);

        User leader = userMapper.selectById(instance.getLeaderId());
        List<GroupBuyParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<GroupBuyParticipant>()
                        .eq(GroupBuyParticipant::getInstanceId, instanceId)
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
                .participants(pVOs)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanExpiredInstances() {
        List<GroupBuyInstance> expired = instanceMapper.selectList(
                new LambdaQueryWrapper<GroupBuyInstance>()
                        .eq(GroupBuyInstance::getStatus, 1)
                        .lt(GroupBuyInstance::getExpireAt, LocalDateTime.now()));

        for (GroupBuyInstance inst : expired) {
            inst.setStatus(3); // 已失败
            instanceMapper.updateById(inst);
            log.info("拼团实例{}已过期，标记为失败", inst.getId());
            // TODO: 退款处理
        }
    }

    private String createGroupBuyOrder(Long uid, GroupBuyActivity activity, Long instanceId,
                                        GroupBuyLaunchDto dto) {
        Product product = productMapper.selectById(activity.getProductId());
        ProductSku sku = skuMapper.selectById(activity.getSkuId());

        String orderNo = OrderNoUtil.gen();
        BigDecimal payAmount = activity.getGroupPrice();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setStatus(0);
        order.setPayStatus(0);
        order.setDeliveryType(dto.getDeliveryType() != null ? dto.getDeliveryType() : 1);
        order.setDeliveryTime(dto.getDeliveryTime());
        order.setGoodsAmount(activity.getGroupPrice());
        order.setPayAmount(payAmount);
        order.setGroupBuyInstanceId(instanceId);
        order.setPayMethod(dto.getPayMethod());
        order.setUserRemark(dto.getUserRemark());

        if (order.getDeliveryType() == 2) {
            order.setPickupPointId(dto.getPickupPointId());
            order.setPickupCode(PickupCodeUtil.gen());
        } else if (dto.getAddressId() != null) {
            UserAddress addr = addressMapper.selectById(dto.getAddressId());
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
        slog.setRemark("拼团下单");
        statusLogMapper.insert(slog);

        return orderNo;
    }

    private String createGroupBuyOrder(Long uid, GroupBuyActivity activity, Long instanceId,
                                        GroupBuyJoinDto dto) {
        Product product = productMapper.selectById(activity.getProductId());
        ProductSku sku = skuMapper.selectById(activity.getSkuId());

        String orderNo = OrderNoUtil.gen();
        BigDecimal payAmount = activity.getGroupPrice();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setStatus(0);
        order.setPayStatus(0);
        order.setDeliveryType(dto.getDeliveryType() != null ? dto.getDeliveryType() : 1);
        order.setDeliveryTime(dto.getDeliveryTime());
        order.setGoodsAmount(activity.getGroupPrice());
        order.setPayAmount(payAmount);
        order.setGroupBuyInstanceId(instanceId);
        order.setPayMethod(dto.getPayMethod());
        order.setUserRemark(dto.getUserRemark());

        if (order.getDeliveryType() == 2) {
            order.setPickupPointId(dto.getPickupPointId());
            order.setPickupCode(PickupCodeUtil.gen());
        } else if (dto.getAddressId() != null) {
            UserAddress addr = addressMapper.selectById(dto.getAddressId());
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
        slog.setRemark("参团下单");
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
