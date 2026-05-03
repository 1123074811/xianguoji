package com.xianguoji.server.module.shop.service;

import com.xianguoji.server.module.shop.vo.DeliverySettingVO;
import com.xianguoji.server.module.shop.vo.PickupPointVO;
import com.xianguoji.server.module.shop.vo.ShopVO;

import java.util.List;

public interface ShopService {

    ShopVO getShopInfo();

    List<PickupPointVO> getPickupPoints();

    DeliverySettingVO getDeliverySetting();
}
