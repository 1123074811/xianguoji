package com.xianguoji.server.module.shop.controller;

import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.shop.service.ShopService;
import com.xianguoji.server.common.cache.PickupGeoService;
import com.xianguoji.server.module.shop.vo.DeliverySettingVO;
import com.xianguoji.server.module.shop.vo.PickupPointVO;
import com.xianguoji.server.module.shop.vo.ShopVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "店铺/配送")
@RestController
@RequestMapping("/api/pub")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final PickupGeoService pickupGeoService;

    @Operation(summary = "店铺基础信息")
    @GetMapping("/shop/info")
    public R<ShopVO> shopInfo() {
        return R.ok(shopService.getShopInfo());
    }

    @Operation(summary = "自提点列表")
    @GetMapping("/pickup-point/list")
    public R<List<PickupPointVO>> pickupPoints() {
        return R.ok(shopService.getPickupPoints());
    }

    @Operation(summary = "自提点GEO排序")
    @GetMapping("/pickup-point/geo")
    public R<List<PickupPointVO>> pickupPointsGeo(
            @RequestParam Double lng,
            @RequestParam Double lat,
            @RequestParam(defaultValue = "50") Double radiusKm) {
        return R.ok(pickupGeoService.sortByDistance(lng, lat, radiusKm));
    }

    @Operation(summary = "配送设置")
    @GetMapping("/delivery-setting")
    public R<DeliverySettingVO> deliverySetting() {
        return R.ok(shopService.getDeliverySetting());
    }
}
