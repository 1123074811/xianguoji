package com.xianguoji.server.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pickup_point")
public class PickupPoint {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String businessHours;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer status;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
