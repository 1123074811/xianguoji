package com.xianguoji.server.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long uid;
    private Long sid;
    private String role;
    private String staffRole;
}
