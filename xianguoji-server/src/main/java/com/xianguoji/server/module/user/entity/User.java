package com.xianguoji.server.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String wxOpenid;
    private String wxUnionid;
    private String tag;
    private Integer status;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
