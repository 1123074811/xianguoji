package com.xianguoji.server.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BindPhoneDto {

    @NotBlank(message = "code不能为空")
    private String code;
}
