package com.xianguoji.server.module.auth.service;

import com.xianguoji.server.module.auth.dto.AdminLoginDto;
import com.xianguoji.server.module.auth.dto.AdminResetPasswordDto;
import com.xianguoji.server.module.auth.dto.SmsLoginDto;
import com.xianguoji.server.module.auth.dto.SmsSendDto;
import com.xianguoji.server.module.auth.dto.WechatLoginDto;
import com.xianguoji.server.module.auth.dto.WechatQuickLoginDto;
import com.xianguoji.server.module.auth.vo.LoginVO;

public interface AuthService {

    void sendSmsCode(SmsSendDto dto);

    LoginVO smsLogin(SmsLoginDto dto);

    LoginVO wechatLogin(WechatLoginDto dto);

    LoginVO quickWechatLogin(WechatQuickLoginDto dto);

    LoginVO adminLogin(AdminLoginDto dto);

    void adminResetPassword(AdminResetPasswordDto dto);
}
