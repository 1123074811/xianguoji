package com.xianguoji.server.module.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import com.xianguoji.server.module.shop.service.ShopService;
import com.xianguoji.server.module.shop.vo.DeliverySettingVO;
import com.xianguoji.server.module.shop.vo.PickupPointVO;
import com.xianguoji.server.module.shop.vo.ShopVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopMapper shopMapper;
    private final PickupPointMapper pickupPointMapper;
    private final DeliverySettingMapper deliverySettingMapper;

    @Override
    @Cacheable(value = "shop", key = "'info'")
    public ShopVO getShopInfo() {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().last("LIMIT 1"));
        if (shop == null) throw new BizException(ResultCode.NOT_FOUND, "店铺信息不存在");
        return ShopVO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .logo(shop.getLogo())
                .description(shop.getDescription())
                .phone(shop.getPhone())
                .address(shop.getAddress())
                .businessHours(shop.getBusinessHours())
                .isOpen(shop.getIsOpen())
                .autoAccept(shop.getAutoAccept())
                .voiceNotify(shop.getVoiceNotify())
                .build();
    }

    @Override
    public List<PickupPointVO> getPickupPoints() {
        List<PickupPoint> list = pickupPointMapper.selectList(
                new LambdaQueryWrapper<PickupPoint>()
                        .eq(PickupPoint::getStatus, 1)
                        .orderByAsc(PickupPoint::getSort));
        return list.stream().map(p -> PickupPointVO.builder()
                .id(p.getId())
                .name(p.getName())
                .address(p.getAddress())
                .phone(p.getPhone())
                .businessHours(p.getBusinessHours())
                .longitude(p.getLongitude())
                .latitude(p.getLatitude())
                .build()).toList();
    }

    @Override
    public DeliverySettingVO getDeliverySetting() {
        DeliverySetting ds = deliverySettingMapper.selectOne(new LambdaQueryWrapper<DeliverySetting>().last("LIMIT 1"));
        if (ds == null) throw new BizException(ResultCode.NOT_FOUND, "配送设置不存在");
        return DeliverySettingVO.builder()
                .minOrderAmount(ds.getMinOrderAmount())
                .baseFee(ds.getBaseFee())
                .freeAmount(ds.getFreeAmount())
                .timeSlots(ds.getTimeSlots())
                .build();
    }
}
