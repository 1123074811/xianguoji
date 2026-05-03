package com.xianguoji.server.module.staff.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StaffVO {

    private Long id;
    private String username;
    private String name;
    private String phone;
    private String avatar;
    private String role;
    private List<String> permissions;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
