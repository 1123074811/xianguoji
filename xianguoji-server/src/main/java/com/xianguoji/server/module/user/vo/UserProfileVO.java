package com.xianguoji.server.module.user.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileVO {

    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private LocalDate birthday;
    private String tag;
    private LocalDateTime registerTime;
    private int couponCount;
    private int favoriteCount;
    private int footprintCount;
    private int groupBuyCount;
}
