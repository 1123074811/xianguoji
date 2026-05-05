package com.xianguoji.server.module.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
