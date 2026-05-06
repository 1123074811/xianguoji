package com.xianguoji.server.module.auth.service;

import com.xianguoji.server.module.auth.dto.AdminLoginDto;
import com.xianguoji.server.module.auth.dto.AdminResetPasswordDto;
import com.xianguoji.server.module.auth.dto.SmsLoginDto;
import com.xianguoji.server.module.auth.dto.SmsSendDto;
import com.xianguoji.server.module.auth.dto.WechatLoginDto;
import com.xianguoji.server.module.auth.dto.WechatQuickLoginDto;
import com.xianguoji.server.module.auth.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    void sendSmsCode(SmsSendDto dto, HttpServletRequest request);

    LoginVO smsLogin(SmsLoginDto dto);

    LoginVO wechatLogin(WechatLoginDto dto);

    LoginVO quickWechatLogin(WechatQuickLoginDto dto);

    LoginVO adminLogin(AdminLoginDto dto, HttpServletRequest request);

    void adminResetPassword(AdminResetPasswordDto dto);

    /** S-7: 用 refresh token 换发新 access + refresh（旋转） */
    LoginVO refreshToken(String refreshToken);
}
