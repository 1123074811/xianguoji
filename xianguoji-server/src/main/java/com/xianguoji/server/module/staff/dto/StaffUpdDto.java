package com.xianguoji.server.module.staff.dto;

import lombok.Data;

import java.util.List;

@Data
public class StaffUpdDto {

    private String name;
    private String phone;
    private String avatar;
    private String role;
    private List<String> permissions;
    private Integer status;
}
