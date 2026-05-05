package com.xianguoji.server.module.auth.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.auth.dto.AdminLoginDto;
import com.xianguoji.server.module.auth.dto.AdminResetPasswordDto;
import com.xianguoji.server.module.auth.dto.SmsLoginDto;
import com.xianguoji.server.module.auth.dto.SmsSendDto;
import com.xianguoji.server.module.auth.dto.WechatLoginDto;
import com.xianguoji.server.module.auth.dto.WechatQuickLoginDto;
import com.xianguoji.server.module.auth.service.AuthService;
import com.xianguoji.server.module.auth.vo.LoginVO;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "鉴权")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StaffMapper staffMapper;

    @Operation(summary = "发送验证码")
    @PostMapping("/api/pub/auth/sms/send")
    public R<Void> sendSmsCode(@Valid @RequestBody SmsSendDto dto) {
        authService.sendSmsCode(dto);
        return R.ok();
    }

    @Operation(summary = "验证码登录")
    @PostMapping("/api/pub/auth/login/sms")
    public R<LoginVO> smsLogin(@Valid @RequestBody SmsLoginDto dto) {
        return R.ok(authService.smsLogin(dto));
    }

    @Operation(summary = "微信小程序登录")
    @PostMapping("/api/pub/auth/login/wechat")
    public R<LoginVO> wechatLogin(@Valid @RequestBody WechatLoginDto dto) {
        return R.ok(authService.wechatLogin(dto));
    }

    @Operation(summary = "微信小程序静默登录（已注册用户复用）")
    @PostMapping("/api/pub/auth/login/wechat/quick")
    public R<LoginVO> quickWechatLogin(@Valid @RequestBody WechatQuickLoginDto dto) {
        return R.ok(authService.quickWechatLogin(dto));
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/api/u/auth/logout")
    @LoginRequired
    public R<Void> userLogout() {
        // JWT 无状态，客户端删除 token 即可
        return R.ok();
    }

    @Operation(summary = "商家账号密码登录")
    @PostMapping("/api/pub/admin/login")
    public R<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDto dto) {
        return R.ok(authService.adminLogin(dto));
    }

    @Operation(summary = "商家重置密码")
    @PostMapping("/api/pub/admin/reset-password")
    public R<Void> adminResetPassword(@Valid @RequestBody AdminResetPasswordDto dto) {
        authService.adminResetPassword(dto);
        return R.ok();
    }

    @Operation(summary = "商家退出")
    @PostMapping("/api/admin/auth/logout")
    @AdminRequired
    public R<Void> adminLogout() {
        return R.ok();
    }

    @Operation(summary = "当前商家信息")
    @GetMapping("/api/admin/auth/me")
    @AdminRequired
    public R<LoginVO.UserInfoVO> adminMe() {
        Long sid = LoginContext.sid();
        Staff staff = staffMapper.selectById(sid);
        if (staff == null) {
            return R.fail(4040, "员工不存在");
        }
        LoginVO.UserInfoVO vo = LoginVO.UserInfoVO.builder()
                .id(staff.getId())
                .nickname(staff.getName())
                .avatar(staff.getAvatar())
                .phone(staff.getPhone())
                .role("staff")
                .staffRole(staff.getRole())
                .build();
        return R.ok(vo);
    }
}
