package com.xianguoji.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginDto {

    @NotBlank(message = "jsCode不能为空")
    private String jsCode;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String avatar;
}
