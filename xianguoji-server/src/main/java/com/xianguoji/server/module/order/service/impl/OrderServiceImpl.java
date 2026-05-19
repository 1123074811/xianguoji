package com.xianguoji.server.module.order.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.enums.OrderStatus;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.common.util.OrderNoUtil;
import com.xianguoji.server.common.util.PickupCodeUtil;
import com.xianguoji.server.common.util.StockRedisHelper;
import com.xianguoji.server.module.cart.entity.CartItem;
import com.xianguoji.server.module.cart.mapper.CartItemMapper;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.order.dto.*;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.entity.OrderStatusLog;
import com.xianguoji.server.module.order.entity.Refund;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.order.mapper.OrderStatusLogMapper;
import com.xianguoji.server.module.order.mapper.RefundMapper;
import com.xianguoji.server.module.order.service.OrderService;
import com.xianguoji.server.module.order.vo.OrderPreviewVO;
import com.xianguoji.server.module.order.vo.OrderVO;
import com.xianguoji.server.module.order.vo.RefundVO;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.promo.service.GroupBuyService;
import com.xianguoji.server.module.promo.service.PromoService;
import com.xianguoji.server.common.event.OrderCreatedEvent;
import com.xianguoji.server.common.event.OrderPaidEvent;
import com.xianguoji.server.common.event.OrderCancelledEvent;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.user.entity.UserAddress;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import com.xianguoji.server.module.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final RefundMapper refundMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final UserAddressMapper addressMapper;
    private final PickupPointMapper pickupPointMapper;
    private final DeliverySettingMapper deliverySettingMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final PromotionRuleMapper promotionRuleMapper;
    private final GroupBuyService groupBuyService;
    private final PromoService promoService;
    private final ShopMapper shopMapper;
    private final WsNotificationService wsNotificationService;
    private final StockRedisHelper stockRedisHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedissonClient redissonClient;

    @Override
    public OrderPreviewVO preview(Long uid, OrderPreviewDto dto) {
        checkShopOpen();
        List<CartItem> cartItems = getSelectedCartItems(uid, dto.getCartItemIds());
        if (cartItems.isEmpty()) throw new BizException(ResultCode.BIZ_ERROR, "购物车为空");

        BigDecimal goodsAmount = BigDecimal.ZERO;
        List<OrderPreviewVO.OrderItemPreview> itemPreviews = new ArrayList<>();
        for (CartItem ci : cartItems) {
            ProductSku sku = skuMapper.selectById(ci.getSkuId());
            Product product = productMapper.selectById(ci.getProductId());
            if (sku == null || product == null || product.getStatus() != 1) continue;
            BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            goodsAmount = goodsAmount.add(subtotal);
            itemPreviews.add(OrderPreviewVO.OrderItemPreview.builder()
                    .skuId(sku.getId())
                    .productName(product.getName())
                    .specName(sku.getSpecName())
                    .image(product.getMainImage())
                    .price(sku.getPrice())
                    .originalPrice(sku.getOriginalPrice())
                    .quantity(ci.getQuantity())
                    .subtotal(subtotal)
                    .build());
        }

        BigDecimal discountAmount = promoService.calculateDiscount(goodsAmount);
        BigDecimal couponAmount = BigDecimal.ZERO;
        if (dto.getUserCouponId() != null) {
            UserCoupon uc = userCouponMapper.selectById(dto.getUserCouponId());
            if (uc != null && uc.getStatus() == 0) {
                Coupon coupon = couponMapper.selectById(uc.getCouponId());
                if (coupon != null && goodsAmount.compareTo(coupon.getMinAmount()) >= 0) {
                    couponAmount = coupon.getAmount();
                }
            }
        }

        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (dto.getDeliveryType() == 1) {
            deliveryFee = calculateDeliveryFee(goodsAmount);
        }

        BigDecimal payAmount = goodsAmount.subtract(discountAmount).subtract(couponAmount).add(deliveryFee);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;

        // 满减提示
        String promotionTip = "";
        List<PromotionRule> rules = promotionRuleMapper.selectList(
                new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getStatus, 1).orderByDesc(PromotionRule::getMinAmount));
        for (PromotionRule rule : rules) {
            if (goodsAmount.compareTo(rule.getMinAmount()) < 0) {
                promotionTip = "再购" + rule.getMinAmount().subtract(goodsAmount) + "元可享" + rule.getName();
                break;
            }
        }

        OrderPreviewVO.AddressVO addressVO = null;
        OrderPreviewVO.PickupPointVO pickupVO = null;
        if (dto.getDeliveryType() == 1) {
            UserAddress addr = null;
            if (dto.getAddressId() != null) {
                addr = addressMapper.selectById(dto.getAddressId());
            }
            // Auto-load default address if no addressId provided
            if (addr == null) {
                addr = addressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, uid)
                        .eq(UserAddress::getIsDefault, 1)
                        .last("LIMIT 1"));
            }
            // Fallback to any address if no default
            if (addr == null) {
                addr = addressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, uid)
                        .last("LIMIT 1"));
            }
            if (addr != null) addressVO = OrderPreviewVO.AddressVO.builder()
                    .id(addr.getId()).consignee(addr.getConsignee()).phone(addr.getPhone())
                    .fullAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail()).build();
        }
        if (dto.getDeliveryType() == 2 && dto.getPickupPointId() != null) {
            PickupPoint pp = pickupPointMapper.selectById(dto.getPickupPointId());
            if (pp != null) pickupVO = OrderPreviewVO.PickupPointVO.builder()
                    .id(pp.getId()).name(pp.getName()).address(pp.getAddress()).build();
        }

        return OrderPreviewVO.builder()
                .items(itemPreviews)
                .goodsAmount(goodsAmount)
                .discountAmount(discountAmount)
                .couponAmount(couponAmount)
                .deliveryFee(deliveryFee)
                .payAmount(payAmount)
                .promotionTip(promotionTip)
                .address(addressVO)
                .pickupPoint(pickupVO)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @com.xianguoji.server.common.annotation.Idempotent(key = "order:submit", ttl = 5, message = "请勿重复提交订单")
    public String submit(Long uid, OrderSubmitDto dto) {
        checkShopOpen();
        // Validate: delivery requires address, pickup requires pickup point
        if (dto.getDeliveryType() == 1 && dto.getAddressId() == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "请选择收货地址");
        }
        if (dto.getDeliveryType() == 2 && dto.getPickupPointId() == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "请选择自提点");
        }
        List<CartItem> cartItems = getSelectedCartItems(uid, dto.getCartItemIds());
        if (cartItems.isEmpty()) throw new BizException(ResultCode.BIZ_ERROR, "购物车为空");

        // 1. 锁库存 + 计算商品总价
        BigDecimal goodsAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        // 记录已 Redis 预扣的 (skuId, quantity)，异常时按实际数量回滚
        java.util.Map<Long, Integer> redisDeducted = new java.util.LinkedHashMap<>();
        try {
            for (CartItem ci : cartItems) {
                ProductSku sku = skuMapper.selectById(ci.getSkuId());
                Product product = productMapper.selectById(ci.getProductId());
                if (sku == null || product == null || product.getStatus() != 1) {
                    throw new BizException(ResultCode.PRODUCT_OFF_SHELF, product != null ? product.getName() + "已下架" : "商品不存在");
                }
                // Redis 预扣库存（活动/抢购场景）
                if (stockRedisHelper.getStock(ci.getSkuId()) >= 0) {
                    if (!stockRedisHelper.deduct(ci.getSkuId(), ci.getQuantity())) {
                        throw new BizException(ResultCode.STOCK_NOT_ENOUGH, product.getName() + " 库存不足");
                    }
                    redisDeducted.merge(ci.getSkuId(), ci.getQuantity(), Integer::sum);
                }
                // DB 乐观锁兜底
                int rows = skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                        .eq(ProductSku::getId, ci.getSkuId())
                        .ge(ProductSku::getStock, ci.getQuantity())
                        .setSql("stock = stock - " + ci.getQuantity() + ", sales = sales + " + ci.getQuantity()));
                if (rows == 0) {
                    throw new BizException(ResultCode.STOCK_NOT_ENOUGH, product.getName() + " 库存不足");
                }

                BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
                goodsAmount = goodsAmount.add(subtotal);

                OrderItem oi = new OrderItem();
                oi.setProductId(product.getId());
                oi.setSkuId(sku.getId());
                oi.setProductName(product.getName());
                oi.setSpecName(sku.getSpecName());
                oi.setImage(product.getMainImage());
                oi.setPrice(sku.getPrice());
                oi.setOriginalPrice(sku.getOriginalPrice());
                oi.setQuantity(ci.getQuantity());
                oi.setSubtotal(subtotal);
                oi.setIsReviewed(0);
                orderItems.add(oi);
            }
        } catch (RuntimeException e) {
            // 下单失败，按实际预扣量回滚 Redis 库存（DB 由 @Transactional 自动回滚）
            for (java.util.Map.Entry<Long, Integer> en : redisDeducted.entrySet()) {
                stockRedisHelper.rollback(en.getKey(), en.getValue());
            }
            throw e;
        }

        // 2. 计算金额
        BigDecimal discountAmount = promoService.calculateDiscount(goodsAmount);
        BigDecimal couponAmount = BigDecimal.ZERO;
        if (dto.getUserCouponId() != null) {
            UserCoupon uc = userCouponMapper.selectById(dto.getUserCouponId());
            if (uc != null && uc.getStatus() == 0 && uc.getUserId().equals(uid)) {
                Coupon coupon = couponMapper.selectById(uc.getCouponId());
                if (coupon != null && goodsAmount.compareTo(coupon.getMinAmount()) >= 0) {
                    couponAmount = coupon.getAmount();
                }
            }
        }
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (dto.getDeliveryType() == 1) {
            deliveryFee = calculateDeliveryFee(goodsAmount);
        }
        BigDecimal payAmount = goodsAmount.subtract(discountAmount).subtract(couponAmount).add(deliveryFee);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;

        // 3. 生成订单
        String orderNo = OrderNoUtil.gen();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setPayStatus(0);
        order.setDeliveryType(dto.getDeliveryType());
        order.setDeliveryTime(dto.getDeliveryTime());
        order.setGoodsAmount(goodsAmount);
        order.setCouponAmount(couponAmount);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryFee(deliveryFee);
        order.setPayAmount(payAmount);
        order.setUserCouponId(dto.getUserCouponId());
        order.setPayMethod(dto.getPayMethod());
        order.setUserRemark(dto.getUserRemark());

        if (dto.getDeliveryType() == 1 && dto.getAddressId() != null) {
            UserAddress addr = addressMapper.selectById(dto.getAddressId());
            if (addr != null) {
                order.setAddressId(addr.getId());
                order.setConsignee(addr.getConsignee());
                order.setConsigneePhone(addr.getPhone());
                order.setConsigneeAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail());
            }
        }
        if (dto.getDeliveryType() == 2) {
            order.setPickupPointId(dto.getPickupPointId());
            order.setPickupCode(PickupCodeUtil.gen());
        }

        orderMapper.insert(order);

        // 4. 写订单项
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        // 5. 状态日志
        addStatusLog(order.getId(), null, OrderStatus.PENDING_ACCEPT.getCode(), 1, uid, "下单并支付");

        // 6. 标记优惠券已使用
        if (dto.getUserCouponId() != null) {
            UserCoupon uc = new UserCoupon();
            uc.setId(dto.getUserCouponId());
            uc.setStatus(1);
            uc.setOrderId(order.getId());
            uc.setUsedAt(LocalDateTime.now());
            userCouponMapper.updateById(uc);
        }

        // 7. 清理购物车
        List<Long> cartIds = cartItems.stream().map(CartItem::getId).toList();
        cartItemMapper.deleteBatchIds(cartIds);

        // 8. 同步商品冗余字段（按 productId 去重，避免一单多 SKU 同商品时重复刷）
        cartItems.stream()
                .map(CartItem::getProductId)
                .distinct()
                .forEach(productId -> {
                    syncProductFields(productId);
                    notifyIfStockWarn(productId);
                });

        // 9. WebSocket通知商家端
        String dtLabel = dto.getDeliveryType() == 1 ? "配送" : "自提";
        wsNotificationService.notifyNewOrder(orderNo, payAmount.toPlainString(), dtLabel);

        // 10. P2-5: 发布订单创建事件（异步处理销量统计等）
        eventPublisher.publishEvent(new OrderCreatedEvent(this, orderNo, order.getId(), uid));

        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object pay(Long uid, String orderNo) {
        RLock lock = redissonClient.getLock("lock:order:pay:" + orderNo);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BizException(ResultCode.PAYMENT_DUPLICATE, "支付处理中，请勿重复提交");
            }
            Order order = getOrderByNo(orderNo);
            if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
            if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
                if (order.getPayStatus() != null && order.getPayStatus() == 1
                        && order.getStatus() != OrderStatus.CANCELLED.getCode()
                        && order.getStatus() != OrderStatus.REFUNDED.getCode()
                        && order.getStatus() != OrderStatus.COMPLETED.getCode()) {
                    return Map.of(
                            "payNo", order.getPayTradeNo() != null ? order.getPayTradeNo() : "",
                            "orderNo", order.getOrderNo(),
                            "payChannel", order.getPayMethod() != null ? order.getPayMethod() : "MOCK",
                            "paidAt", order.getPayTime() != null ? order.getPayTime() : "",
                            "status", order.getStatus()
                    );
                }
                throw new BizException(ResultCode.ORDER_STATUS_INVALID, "订单状态不允许支付");
            }
            LocalDateTime paidAt = LocalDateTime.now();
            String payNo = "MOCK" + OrderNoUtil.gen();
            order.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
            order.setPayStatus(1);
            order.setPayMethod("MOCK");
            order.setPayTradeNo(payNo);
            order.setPayTime(paidAt);
            orderMapper.updateById(order);
            addStatusLog(order.getId(), OrderStatus.PENDING_PAY.getCode(), OrderStatus.PENDING_ACCEPT.getCode(), 1, uid, "模拟支付成功");
            eventPublisher.publishEvent(new OrderPaidEvent(this, orderNo, order.getId(), order.getPayAmount().toPlainString(), order.getDeliveryType()));
            return Map.of(
                    "payNo", payNo,
                    "orderNo", order.getOrderNo(),
                    "payChannel", "MOCK",
                    "paidAt", paidAt,
                    "status", order.getStatus()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.BIZ_ERROR, "支付处理中断，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public PageVO<OrderVO> userOrderPage(Long uid, OrderQry qry) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, uid);
        applyTabFilter(wrapper, qry.getTab());
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> page = orderMapper.selectPage(new Page<>(qry.getPage(), qry.getSize()), wrapper);
        List<OrderVO> voList = page.getRecords().stream().map(this::toOrderVO).toList();
        return new PageVO<>(page.getTotal(), voList, qry.getPage(), qry.getSize());
    }

    @Override
    public OrderVO getOrderDetail(Long uid, String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
        return toOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long uid, String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
        if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "只能取消待付款订单");
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason("用户主动取消");
        orderMapper.updateById(order);
        addStatusLog(order.getId(), OrderStatus.PENDING_PAY.getCode(), OrderStatus.CANCELLED.getCode(), 1, uid, "用户取消");

        // 释放库存
        releaseStock(order.getId());

        // P2-5: 发布订单取消事件（异步回退销量等）
        eventPublisher.publishEvent(new OrderCancelledEvent(this, orderNo, order.getId(), "用户主动取消"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long uid, String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
        if (order.getStatus() != OrderStatus.DELIVERING.getCode() && order.getStatus() != OrderStatus.PENDING_PICKUP.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "当前状态不可确认收货");
        }
        int fromStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setFinishedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addStatusLog(order.getId(), fromStatus, OrderStatus.COMPLETED.getCode(), 1, uid, "确认收货");
    }

    @Override
    public void remindShip(Long uid, String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
        wsNotificationService.notifyRemindShip(orderNo);
        log.info("用户{}提醒发货，订单{}", uid, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repurchase(Long uid, String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem oi : items) {
            CartItem existing = cartItemMapper.selectOne(
                    new LambdaQueryWrapper<CartItem>()
                            .eq(CartItem::getUserId, uid)
                            .eq(CartItem::getSkuId, oi.getSkuId()));
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + oi.getQuantity());
                cartItemMapper.updateById(existing);
            } else {
                CartItem ci = new CartItem();
                ci.setUserId(uid);
                ci.setProductId(oi.getProductId());
                ci.setSkuId(oi.getSkuId());
                ci.setQuantity(oi.getQuantity());
                ci.setSelected(1);
                cartItemMapper.insert(ci);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long uid, RefundApplyDto dto) {
        Order order = getOrderByNo(dto.getOrderNo());
        if (!order.getUserId().equals(uid)) throw new BizException(ResultCode.ACCESS_DENIED);
        if (!isRefundableStatus(order.getStatus())) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "订单状态不允许退款");
        }

        Refund existing = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getOrderId, order.getId())
                .in(Refund::getStatus, 0, 1, 3)
                .last("LIMIT 1"));
        if (existing != null) {
            return;
        }

        int fromStatus = order.getStatus();
        Refund refund = new Refund();
        refund.setRefundNo(OrderNoUtil.genRefundNo());
        refund.setOrderId(order.getId());
        refund.setUserId(uid);
        refund.setType(dto.getType());
        refund.setAmount(order.getPayAmount());
        refund.setReason(dto.getReason());
        refund.setImages(dto.getImages());
        refund.setStatus(0);
        refundMapper.insert(refund);

        order.setStatus(OrderStatus.REFUNDING.getCode());
        orderMapper.updateById(order);
        addStatusLog(order.getId(), fromStatus, OrderStatus.REFUNDING.getCode(), 1, uid, "申请退款");

        // WebSocket通知商家端
        wsNotificationService.notifyRefundApply(order.getOrderNo(), order.getPayAmount().toPlainString());
    }

    @Override
    public RefundVO getRefundDetail(Long uid, String refundNo) {
        Refund refund = refundMapper.selectOne(
                new LambdaQueryWrapper<Refund>().eq(Refund::getRefundNo, refundNo));
        if (refund == null || !refund.getUserId().equals(uid)) throw new BizException(ResultCode.NOT_FOUND);
        Order order = orderMapper.selectById(refund.getOrderId());
        return RefundVO.builder()
                .id(refund.getId())
                .refundNo(refund.getRefundNo())
                .orderNo(order != null ? order.getOrderNo() : "")
                .type(refund.getType())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .images(refund.getImages())
                .status(refund.getStatus())
                .refundChannel(refund.getRefundChannel())
                .refundTransactionId(refund.getRefundTransactionId())
                .rejectReason(refund.getRejectReason())
                .handledAt(refund.getHandledAt())
                .createdAt(refund.getCreatedAt())
                .build();
    }

    // ===== 商家端 =====

    @Override
    public PageVO<OrderVO> adminOrderPage(AdminOrderQry qry) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (qry.getStatus() != null) wrapper.eq(Order::getStatus, qry.getStatus());
        if (qry.getKeyword() != null && !qry.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Order::getOrderNo, qry.getKeyword())
                    .or().like(Order::getConsignee, qry.getKeyword()));
        }
        if (qry.getStartDate() != null) {
            wrapper.ge(Order::getCreatedAt, qry.getStartDate() + " 00:00:00");
        }
        if (qry.getEndDate() != null) {
            wrapper.le(Order::getCreatedAt, qry.getEndDate() + " 23:59:59");
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> page = orderMapper.selectPage(new Page<>(qry.getPage(), qry.getSize()), wrapper);
        List<OrderVO> voList = page.getRecords().stream().map(this::toOrderVO).toList();
        return new PageVO<>(page.getTotal(), voList, qry.getPage(), qry.getSize());
    }

    @Override
    public OrderVO adminOrderDetail(String orderNo) {
        return toOrderVO(getOrderByNo(orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != OrderStatus.PENDING_ACCEPT.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "只能接单待接单状态订单");
        }
        order.setStatus(OrderStatus.PREPARING.getCode());
        orderMapper.updateById(order);
        addStatusLog(order.getId(), OrderStatus.PENDING_ACCEPT.getCode(), OrderStatus.PREPARING.getCode(), 2, null, "商家接单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(String orderNo, String reason) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != OrderStatus.PENDING_ACCEPT.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(reason);
        orderMapper.updateById(order);
        addStatusLog(order.getId(), OrderStatus.PENDING_ACCEPT.getCode(), OrderStatus.CANCELLED.getCode(), 2, null, "商家拒单: " + reason);
        // TODO: 自动退款
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, Integer deliveryType, String courierName, String courierPhone) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != OrderStatus.PREPARING.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID);
        }
        int toStatus = deliveryType != null && deliveryType == 2
                ? OrderStatus.PENDING_PICKUP.getCode()
                : OrderStatus.DELIVERING.getCode();
        order.setStatus(toStatus);
        order.setCourierName(courierName);
        order.setCourierPhone(courierPhone);
        order.setDeliveredAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addStatusLog(order.getId(), OrderStatus.PREPARING.getCode(), toStatus, 2, null, "标记出库");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickupVerify(String orderNo, String pickupCode) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != OrderStatus.PENDING_PICKUP.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID);
        }
        if (!pickupCode.equals(order.getPickupCode())) {
            throw new BizException(ResultCode.BIZ_ERROR, "自提码不正确");
        }
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setFinishedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addStatusLog(order.getId(), OrderStatus.PENDING_PICKUP.getCode(), OrderStatus.COMPLETED.getCode(), 2, null, "核销自提码");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != OrderStatus.DELIVERING.getCode()) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "只能完成配送中的订单");
        }
        int fromStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setFinishedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addStatusLog(order.getId(), fromStatus, OrderStatus.COMPLETED.getCode(), 2, null, "商家确认送达");
    }

    @Override
    public Object printReceipt(String orderNo) {
        Order order = getOrderByNo(orderNo);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        return Map.of("orderNo", orderNo, "items", items, "payAmount", order.getPayAmount(),
                "createdAt", order.getCreatedAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(String refundNo) {
        RLock lock = redissonClient.getLock("lock:order:refund:" + refundNo);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BizException(ResultCode.BIZ_ERROR, "退款处理中，请勿重复提交");
            }
            Refund refund = refundMapper.selectOne(
                    new LambdaQueryWrapper<Refund>().eq(Refund::getRefundNo, refundNo));
            if (refund == null) throw new BizException(ResultCode.NOT_FOUND, "退款单不存在");
            if (refund.getStatus() == 3) return;
            if (refund.getStatus() != 0 && refund.getStatus() != 1) {
                throw new BizException(ResultCode.BIZ_ERROR, "退款单状态不正确");
            }

            refund.setStatus(3);
            refund.setRefundChannel("MOCK");
            refund.setRefundTransactionId("MOCK_REFUND_" + refund.getRefundNo());
            refund.setHandledBy(LoginContext.sid());
            refund.setHandledAt(LocalDateTime.now());
            refundMapper.updateById(refund);

            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null) {
                if (order.getStatus() == OrderStatus.REFUNDED.getCode()) return;
                int fromStatus = order.getStatus();
                order.setPayStatus(2);
                order.setStatus(OrderStatus.REFUNDED.getCode());
                orderMapper.updateById(order);
                addStatusLog(order.getId(), fromStatus, OrderStatus.REFUNDED.getCode(), 2, null, "模拟退款完成");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.BIZ_ERROR, "退款处理中断，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(String refundNo, String reason) {
        Refund refund = refundMapper.selectOne(
                new LambdaQueryWrapper<Refund>().eq(Refund::getRefundNo, refundNo));
        if (refund == null) throw new BizException(ResultCode.NOT_FOUND);
        if (refund.getStatus() != 0) throw new BizException(ResultCode.BIZ_ERROR);

        refund.setStatus(2);
        refund.setRejectReason(reason);
        refund.setHandledBy(LoginContext.sid());
        refund.setHandledAt(LocalDateTime.now());
        refundMapper.updateById(refund);

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order != null) {
            int fromStatus = order.getStatus();
            OrderStatusLog refundApplyLog = statusLogMapper.selectOne(new LambdaQueryWrapper<OrderStatusLog>()
                    .eq(OrderStatusLog::getOrderId, order.getId())
                    .eq(OrderStatusLog::getToStatus, OrderStatus.REFUNDING.getCode())
                    .orderByDesc(OrderStatusLog::getId)
                    .last("LIMIT 1"));
            int restoreStatus = refundApplyLog != null && refundApplyLog.getFromStatus() != null
                    ? refundApplyLog.getFromStatus()
                    : OrderStatus.PENDING_ACCEPT.getCode();
            order.setStatus(restoreStatus);
            orderMapper.updateById(order);
            addStatusLog(order.getId(), fromStatus, restoreStatus, 2, null, "商家拒绝退款: " + reason);
        }
    }

    @Override
    public int newOrderCount(long since) {
        return Math.toIntExact(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, OrderStatus.PENDING_ACCEPT.getCode())
                        .gt(Order::getCreatedAt, new java.sql.Timestamp(since).toLocalDateTime())));
    }

    // ===== 私有方法 =====

    private void checkShopOpen() {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().last("LIMIT 1"));
        if (shop == null || shop.getIsOpen() == null || shop.getIsOpen() != 1) {
            throw new BizException(ResultCode.SHOP_CLOSED);
        }
    }

    private Order getOrderByNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        return order;
    }

    private List<CartItem> getSelectedCartItems(Long uid, List<Long> cartItemIds) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, uid)
                .eq(CartItem::getSelected, 1);
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            wrapper.in(CartItem::getId, cartItemIds);
        }
        return cartItemMapper.selectList(wrapper);
    }

    private BigDecimal calculateDeliveryFee(BigDecimal goodsAmount) {
        DeliverySetting ds = deliverySettingMapper.selectOne(new LambdaQueryWrapper<DeliverySetting>().last("LIMIT 1"));
        if (ds == null) return BigDecimal.ZERO;
        if (ds.getFreeAmount().compareTo(BigDecimal.ZERO) > 0 && goodsAmount.compareTo(ds.getFreeAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        return ds.getBaseFee();
    }

    private void addStatusLog(Long orderId, Integer fromStatus, Integer toStatus, int operatorType, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        statusLogMapper.insert(log);
    }

    private void releaseStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem oi : items) {
            skuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                    .eq(ProductSku::getId, oi.getSkuId())
                    .setSql("stock = stock + " + oi.getQuantity() + ", sales = GREATEST(sales - " + oi.getQuantity() + ", 0)"));
            syncProductFields(oi.getProductId());
        }
    }

    private void syncProductFields(Long productId) {
        Product p = new Product();
        p.setId(productId);
        p.setMinPrice(skuMapper.selectMinPrice(productId));
        p.setMaxPrice(skuMapper.selectMaxPrice(productId));
        p.setTotalStock(skuMapper.selectTotalStock(productId));
        productMapper.updateById(p);
    }

    private void notifyIfStockWarn(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) return;
        if (product.getTotalStock() == null || product.getStockWarnThreshold() == null) return;
        if (product.getTotalStock() <= product.getStockWarnThreshold()) {
            wsNotificationService.notifyStockWarn(product.getId(), product.getName(), product.getTotalStock(), product.getStockWarnThreshold());
        }
    }

    private void applyTabFilter(LambdaQueryWrapper<Order> wrapper, String tab) {
        if (tab == null) return;
        switch (tab) {
            case "pending" -> wrapper.eq(Order::getStatus, OrderStatus.PENDING_PAY.getCode())
                    .or().eq(Order::getStatus, OrderStatus.PENDING_ACCEPT.getCode());
            case "processing" -> wrapper.eq(Order::getStatus, OrderStatus.PREPARING.getCode());
            case "delivering" -> wrapper.in(Order::getStatus,
                    OrderStatus.DELIVERING.getCode(), OrderStatus.PENDING_PICKUP.getCode());
            case "done" -> wrapper.eq(Order::getStatus, OrderStatus.COMPLETED.getCode());
            case "aftersale" -> wrapper.in(Order::getStatus,
                    OrderStatus.REFUNDING.getCode(), OrderStatus.REFUNDED.getCode());
        }
    }

    private boolean isRefundableStatus(Integer status) {
        if (status == null) return false;
        return status == OrderStatus.PENDING_ACCEPT.getCode()
                || status == OrderStatus.PREPARING.getCode()
                || status == OrderStatus.DELIVERING.getCode()
                || status == OrderStatus.PENDING_PICKUP.getCode()
                || status == OrderStatus.COMPLETED.getCode();
    }

    private OrderVO toOrderVO(Order order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        List<OrderVO.OrderItemVO> itemVOs = items.stream().map(oi -> OrderVO.OrderItemVO.builder()
                .id(oi.getId())
                .productId(oi.getProductId())
                .skuId(oi.getSkuId())
                .productName(oi.getProductName())
                .specName(oi.getSpecName())
                .image(oi.getImage())
                .price(oi.getPrice())
                .originalPrice(oi.getOriginalPrice())
                .quantity(oi.getQuantity())
                .subtotal(oi.getSubtotal())
                .isReviewed(oi.getIsReviewed())
                .build()).toList();

        return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .payStatus(order.getPayStatus())
                .deliveryType(order.getDeliveryType())
                .deliveryTime(order.getDeliveryTime())
                .consignee(order.getConsignee())
                .consigneePhone(order.getConsigneePhone())
                .consigneeAddress(order.getConsigneeAddress())
                .pickupCode(order.getPickupCode())
                .goodsAmount(order.getGoodsAmount())
                .couponAmount(order.getCouponAmount())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .payAmount(order.getPayAmount())
                .payMethod(order.getPayMethod())
                .payTradeNo(order.getPayTradeNo())
                .userRemark(order.getUserRemark())
                .cancelReason(order.getCancelReason())
                .groupBuyInstanceId(order.getGroupBuyInstanceId())
                .groupBuyInstance(order.getGroupBuyInstanceId() != null ? groupBuyService.getInstanceDetail(order.getGroupBuyInstanceId()) : null)
                .payTime(order.getPayTime())
                .deliveredAt(order.getDeliveredAt())
                .finishedAt(order.getFinishedAt())
                .createdAt(order.getCreatedAt())
                .items(itemVOs)
                .build();
    }
}
