package com.xianguoji.server.module.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.shop.entity.AdminNotification;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.entity.NotifySetting;
import com.xianguoji.server.common.cache.PickupGeoService;
import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.shop.mapper.AdminNotificationMapper;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import com.xianguoji.server.module.shop.mapper.NotifySettingMapper;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "店铺管理-商家端")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopMapper shopMapper;
    private final PickupPointMapper pickupPointMapper;
    private final PickupGeoService pickupGeoService;
    private final DeliverySettingMapper deliverySettingMapper;
    private final NotifySettingMapper notifySettingMapper;
    private final AdminNotificationMapper adminNotificationMapper;
    private final WsNotificationService wsNotificationService;

    @Operation(summary = "获取店铺信息")
    @GetMapping("/shop/info")
    @AdminRequired
    public R<Shop> getShopInfo() {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().last("LIMIT 1"));
        return R.ok(shop);
    }

    @Operation(summary = "修改店铺信息")
    @PutMapping("/shop/info")
    @AdminRequired
    @CacheEvict(value = "shop", allEntries = true)
    public R<Void> updateShopInfo(@RequestBody Shop dto) {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().last("LIMIT 1"));
        if (shop == null) return R.fail(4040, "店铺不存在");
        dto.setId(shop.getId());
        shopMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "切换营业状态")
    @PutMapping("/shop/open-status")
    @AdminRequired
    @CacheEvict(value = "shop", allEntries = true)
    public R<Void> toggleOpenStatus(@RequestBody Map<String, Integer> body) {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().last("LIMIT 1"));
        if (shop == null) return R.fail(4040, "店铺不存在");
        shop.setIsOpen(body.get("isOpen"));
        shopMapper.updateById(shop);
        return R.ok();
    }

    // ===== 自提点 =====
    @Operation(summary = "自提点列表")
    @GetMapping("/pickup-point/list")
    @AdminRequired
    public R<List<PickupPoint>> pickupPointList() {
        return R.ok(pickupPointMapper.selectList(new LambdaQueryWrapper<PickupPoint>().orderByAsc(PickupPoint::getSort)));
    }

    @Operation(summary = "新增自提点")
    @PostMapping("/pickup-point")
    @AdminRequired
    public R<Void> addPickupPoint(@RequestBody PickupPoint dto) {
        pickupPointMapper.insert(dto);
        pickupGeoService.rebuildGeoIndex();
        return R.ok();
    }

    @Operation(summary = "更新自提点")
    @PutMapping("/pickup-point/{id}")
    @AdminRequired
    public R<Void> updatePickupPoint(@PathVariable Long id, @RequestBody PickupPoint dto) {
        dto.setId(id);
        pickupPointMapper.updateById(dto);
        pickupGeoService.rebuildGeoIndex();
        return R.ok();
    }

    @Operation(summary = "删除自提点")
    @DeleteMapping("/pickup-point/{id}")
    @AdminRequired
    public R<Void> deletePickupPoint(@PathVariable Long id) {
        pickupPointMapper.deleteById(id);
        pickupGeoService.rebuildGeoIndex();
        return R.ok();
    }

    // ===== 配送设置 =====
    @Operation(summary = "获取配送设置")
    @GetMapping("/delivery-setting")
    @AdminRequired
    public R<DeliverySetting> getDeliverySetting() {
        DeliverySetting ds = deliverySettingMapper.selectOne(new LambdaQueryWrapper<DeliverySetting>().last("LIMIT 1"));
        return R.ok(ds);
    }

    @Operation(summary = "修改配送设置")
    @PutMapping("/delivery-setting")
    @AdminRequired
    public R<Void> updateDeliverySetting(@RequestBody DeliverySetting dto) {
        DeliverySetting ds = deliverySettingMapper.selectOne(new LambdaQueryWrapper<DeliverySetting>().last("LIMIT 1"));
        if (ds == null) {
            deliverySettingMapper.insert(dto);
        } else {
            dto.setId(ds.getId());
            deliverySettingMapper.updateById(dto);
        }
        return R.ok();
    }

    // ===== 通知设置 =====
    @Operation(summary = "通知设置列表")
    @GetMapping("/notify-setting/list")
    @AdminRequired
    public R<List<NotifySetting>> notifySettingList() {
        return R.ok(notifySettingMapper.selectList(null));
    }

    @Operation(summary = "更新通知设置")
    @PutMapping("/notify-setting/{id}")
    @AdminRequired
    public R<Void> updateNotifySetting(@PathVariable Integer id, @RequestBody NotifySetting dto) {
        dto.setId(id);
        notifySettingMapper.updateById(dto);
        return R.ok();
    }

    // ===== 商家通知 =====
    @Operation(summary = "通知列表")
    @GetMapping("/notification/list")
    @AdminRequired
    public R<List<AdminNotification>> notificationList(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<AdminNotification> wrapper = new LambdaQueryWrapper<AdminNotification>()
                .orderByDesc(AdminNotification::getCreatedAt);
        if (type != null) {
            wrapper.eq(AdminNotification::getType, type);
        }
        return R.ok(adminNotificationMapper.selectList(wrapper));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notification/{id}/read")
    @AdminRequired
    public R<Void> markRead(@PathVariable Long id) {
        AdminNotification n = adminNotificationMapper.selectById(id);
        if (n != null) {
            n.setIsRead(1);
            adminNotificationMapper.updateById(n);
        }
        return R.ok();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/notification/read-all")
    @AdminRequired
    public R<Void> markAllRead() {
        List<AdminNotification> list = adminNotificationMapper.selectList(
                new LambdaQueryWrapper<AdminNotification>().eq(AdminNotification::getIsRead, 0));
        for (AdminNotification n : list) {
            n.setIsRead(1);
            adminNotificationMapper.updateById(n);
        }
        return R.ok();
    }

    @Operation(summary = "未读数")
    @GetMapping("/notification/unread-count")
    @AdminRequired
    public R<Long> unreadCount() {
        return R.ok(adminNotificationMapper.selectCount(
                new LambdaQueryWrapper<AdminNotification>().eq(AdminNotification::getIsRead, 0)));
    }

    @Operation(summary = "发布系统通知")
    @PostMapping("/notification/system")
    @AdminRequired
    public R<Void> publishSystemNotification(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "系统通知");
        String content = body.getOrDefault("content", "");
        String linkUrl = body.get("linkUrl");
        wsNotificationService.notifySystem(title, content, linkUrl);
        return R.ok();
    }
}
