package com.xianguoji.server.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {

    @TableId(type = IdType.AUTO)
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
