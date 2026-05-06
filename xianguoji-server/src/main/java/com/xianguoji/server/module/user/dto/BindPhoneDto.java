package com.xianguoji.server.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BindPhoneDto {

    @NotBlank(message = "jsCode不能为空")
    private String jsCode;

    @NotBlank(message = "encryptedData不能为空")
    private String encryptedData;

    @NotBlank(message = "iv不能为空")
    private String iv;
}
