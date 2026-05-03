package com.xianguoji.server.module.shop.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopVO {

    private Integer id;
    private String name;
    private String logo;
    private String description;
    private String phone;
    private String address;
    private String businessHours;
    private Integer isOpen;
    private Integer autoAccept;
    private Integer voiceNotify;
}
