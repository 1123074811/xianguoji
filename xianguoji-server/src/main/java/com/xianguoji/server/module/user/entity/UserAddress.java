package com.xianguoji.server.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_address")
public class UserAddress {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String tag;
    private Integer isDefault;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
