package com.xianguoji.server.module.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StaffAddDto {

    @NotBlank(message = "登录账号不能为空")
    @Size(max = 32)
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;
    private String avatar;
    @NotBlank(message = "角色不能为空")
    private String role;
    private List<String> permissions;
}
