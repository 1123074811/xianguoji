package com.xianguoji.server.module.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.entity.NotifySetting;
import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import com.xianguoji.server.module.shop.mapper.NotifySettingMapper;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    private final DeliverySettingMapper deliverySettingMapper;
    private final NotifySettingMapper notifySettingMapper;

    @Operation(summary = "修改店铺信息")
    @PutMapping("/shop/info")
    @AdminRequired
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
        return R.ok();
    }

    @Operation(summary = "更新自提点")
    @PutMapping("/pickup-point/{id}")
    @AdminRequired
    public R<Void> updatePickupPoint(@PathVariable Long id, @RequestBody PickupPoint dto) {
        dto.setId(id);
        pickupPointMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除自提点")
    @DeleteMapping("/pickup-point/{id}")
    @AdminRequired
    public R<Void> deletePickupPoint(@PathVariable Long id) {
        pickupPointMapper.deleteById(id);
        return R.ok();
    }

    // ===== 配送设置 =====
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
}
