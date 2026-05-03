package com.xianguoji.server.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdDto {

    @Size(max = 64, message = "昵称最长64字")
    private String nickname;

    private String avatar;

    private Integer gender;

    private LocalDate birthday;
}
