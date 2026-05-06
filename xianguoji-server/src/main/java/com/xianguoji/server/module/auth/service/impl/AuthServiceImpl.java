package com.xianguoji.server.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.RateLimit;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.JwtBlacklistManager;
import com.xianguoji.server.common.security.JwtUtil;
import com.xianguoji.server.common.security.LoginAttemptManager;
import com.xianguoji.server.common.security.SmsRateLimiter;
import com.xianguoji.server.common.util.IpUtil;
import com.xianguoji.server.common.util.SmsUtil;
import com.xianguoji.server.common.util.WechatUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final StaffMapper staffMapper;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistManager jwtBlacklistManager;
    private final SmsUtil smsUtil;
    private final WechatUtil wechatUtil;
    private final LoginAttemptManager loginAttemptManager;
    private final SmsRateLimiter smsRateLimiter;

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CODE_TTL_MINUTES = 5;
    private static final int DAILY_LIMIT = 10;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Value("${xianguoji.sms.provider:mock}")
    private String smsProvider;

    /** 开发态固定验证码，与 docs/backend-spec.md §11 一致 */
    private static final String DEV_FIXED_CODE = "1234";

    @Override
    @RateLimit(key = "sms", limit = 5, period = 60)
    public void sendSmsCode(SmsSendDto dto, HttpServletRequest request) {
        String phone = dto.getPhone();
        String ip = IpUtil.getClientIp(request);

        // S-4: 统一短信频控（手机号60s/天10次 + IP 60s 5次）
        smsRateLimiter.check(phone, ip);

        String code = "mock".equals(smsProvider) ? DEV_FIXED_CODE : RandomUtil.randomNumbers(4);
        stringRedisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // S-4: 发送成功后标记60s冷却
        smsRateLimiter.markSent(phone);

        smsUtil.send(phone, code);
    }

    @Override
    public LoginVO smsLogin(SmsLoginDto dto) {
        String phone = dto.getPhone();
        String redisCode = stringRedisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
        if (redisCode == null || !redisCode.equals(dto.getCode())) {
            throw new BizException(ResultCode.BIZ_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(SMS_CODE_PREFIX + phone);

        // 查找或注册用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        boolean isNew = false;
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(7));
            user.setTag("new");
            user.setStatus(1);
            user.setRegisterTime(LocalDateTime.now());
            userMapper.insert(user);
            isNew = true;
        }
        if (user.getStatus() == 0) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账号已被禁用");
        }

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        if (!isNew && "new".equals(user.getTag())) {
            // 首次登录后标记 tag=new 保持不变，后续由定时任务更新
        }
        userMapper.updateById(user);

        String accessToken = jwtUtil.issueUserAccessToken(user.getId(), "");
        String refreshToken = jwtUtil.issueUserRefreshToken(user.getId());
        LocalDateTime accessExpireAt = LocalDateTime.now().plusMinutes(jwtUtil.getUserAccessTtlMinutes());
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusHours(jwtUtil.getUserRefreshTtlHours());

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(accessToken, user.getId(), (long) jwtUtil.getUserAccessTtlMinutes() * 60_000);

        return LoginVO.builder()
                .token(accessToken)
                .expireAt(accessExpireAt)
                .refreshToken(refreshToken)
                .refreshExpireAt(refreshExpireAt)
                .userInfo(LoginVO.UserInfoVO.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(maskPhone(phone))
                        .role("user")
                        .isNew(isNew)
                        .build())
                .build();
    }

    @Override
    public LoginVO quickWechatLogin(WechatQuickLoginDto dto) {
        cn.hutool.json.JSONObject session = wechatUtil.code2Session(dto.getJsCode());
        String openid = session.getStr("openid");
        if (openid == null || openid.isEmpty()) {
            throw new BizException(ResultCode.BIZ_ERROR, "微信登录失败：未获取到openid");
        }
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getWxOpenid, openid));
        // 用户不存在 或 资料未补全 → 通知前端弹授权框
        if (user == null || user.getNickname() == null || user.getNickname().isBlank()) {
            throw new BizException(ResultCode.WX_PROFILE_REQUIRED, "请补全微信资料");
        }
        if (user.getStatus() == 0) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账号已被禁用");
        }
        String unionid = session.getStr("unionid");
        user.setLastLoginTime(LocalDateTime.now());
        if (unionid != null && !unionid.equals(user.getWxUnionid())) {
            user.setWxUnionid(unionid);
        }
        userMapper.updateById(user);

        String accessToken = jwtUtil.issueUserAccessToken(user.getId(), "");
        String refreshToken = jwtUtil.issueUserRefreshToken(user.getId());
        LocalDateTime accessExpireAt = LocalDateTime.now().plusMinutes(jwtUtil.getUserAccessTtlMinutes());
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusHours(jwtUtil.getUserRefreshTtlHours());

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(accessToken, user.getId(), (long) jwtUtil.getUserAccessTtlMinutes() * 60_000);

        return LoginVO.builder()
                .token(accessToken)
                .expireAt(accessExpireAt)
                .refreshToken(refreshToken)
                .refreshExpireAt(refreshExpireAt)
                .userInfo(LoginVO.UserInfoVO.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhone() != null ? maskPhone(user.getPhone()) : null)
                        .role("user")
                        .isNew(false)
                        .build())
                .build();
    }

    @Override
    public LoginVO wechatLogin(WechatLoginDto dto) {
        // 1. 调用微信 code2Session 获取 openid
        cn.hutool.json.JSONObject session = wechatUtil.code2Session(dto.getJsCode());
        String openid = session.getStr("openid");
        if (openid == null || openid.isEmpty()) {
            throw new BizException(ResultCode.BIZ_ERROR, "微信登录失败：未获取到openid");
        }
        String unionid = session.getStr("unionid");

        // 2. 根据 openid 查找或注册用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getWxOpenid, openid));
        boolean isNew = false;
        if (user == null) {
            user = new User();
            user.setWxOpenid(openid);
            user.setWxUnionid(unionid);
            user.setNickname(dto.getNickname());
            user.setAvatar(dto.getAvatar() != null && !dto.getAvatar().isEmpty() ? dto.getAvatar() : null);
            user.setTag("new");
            user.setStatus(1);
            user.setRegisterTime(LocalDateTime.now());
            userMapper.insert(user);
            isNew = true;
        }
        if (user.getStatus() == 0) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账号已被禁用");
        }

        // 3. 更新昵称、头像、登录时间 & unionid
        user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null && !dto.getAvatar().isEmpty()) {
            user.setAvatar(dto.getAvatar());
        }
        user.setLastLoginTime(LocalDateTime.now());
        if (unionid != null && !unionid.equals(user.getWxUnionid())) {
            user.setWxUnionid(unionid);
        }
        userMapper.updateById(user);

        // 4. 签发 JWT（双 token）
        String accessToken = jwtUtil.issueUserAccessToken(user.getId(), "");
        String refreshToken = jwtUtil.issueUserRefreshToken(user.getId());
        LocalDateTime accessExpireAt = LocalDateTime.now().plusMinutes(jwtUtil.getUserAccessTtlMinutes());
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusHours(jwtUtil.getUserRefreshTtlHours());

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(accessToken, user.getId(), (long) jwtUtil.getUserAccessTtlMinutes() * 60_000);

        return LoginVO.builder()
                .token(accessToken)
                .expireAt(accessExpireAt)
                .refreshToken(refreshToken)
                .refreshExpireAt(refreshExpireAt)
                .userInfo(LoginVO.UserInfoVO.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhone() != null ? maskPhone(user.getPhone()) : null)
                        .role("user")
                        .isNew(isNew)
                        .build())
                .build();
    }

    @Override
    public LoginVO adminLogin(AdminLoginDto dto, HttpServletRequest request) {
        String ip = IpUtil.getClientIp(request);
        String account = dto.getUsername();

        // S-4: 检查账户/IP是否已被锁定
        loginAttemptManager.checkLocked(account, ip);

        // 校验图形验证码
        String redisCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + dto.getCaptchaKey());
        if (redisCode == null || !redisCode.equals(dto.getCaptchaCode().toLowerCase())) {
            throw new BizException(ResultCode.BIZ_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + dto.getCaptchaKey());

        Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, dto.getUsername()));
        if (staff == null) {
            loginAttemptManager.recordFail(account, ip);
            throw new BizException(ResultCode.BIZ_ERROR, "账号或密码错误");
        }
        if (staff.getStatus() == 0) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), staff.getPasswordHash())) {
            loginAttemptManager.recordFail(account, ip);
            throw new BizException(ResultCode.BIZ_ERROR, "账号或密码错误");
        }

        // S-4: 登录成功，重置失败计数
        loginAttemptManager.reset(account, ip);

        staff.setLastLoginAt(LocalDateTime.now());
        staffMapper.updateById(staff);

        String accessToken = jwtUtil.issueAdminAccessToken(staff.getId(), staff.getRole(), "");
        String refreshToken = jwtUtil.issueAdminRefreshToken(staff.getId(), staff.getRole());
        LocalDateTime accessExpireAt = LocalDateTime.now().plusMinutes(jwtUtil.getAdminAccessTtlMinutes());
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusHours(jwtUtil.getAdminRefreshTtlHours());

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(accessToken, staff.getId(), (long) jwtUtil.getAdminAccessTtlMinutes() * 60_000);

        return LoginVO.builder()
                .token(accessToken)
                .expireAt(accessExpireAt)
                .refreshToken(refreshToken)
                .refreshExpireAt(refreshExpireAt)
                .userInfo(LoginVO.UserInfoVO.builder()
                        .id(staff.getId())
                        .nickname(staff.getName())
                        .avatar(staff.getAvatar())
                        .phone(maskPhone(staff.getPhone()))
                        .role("staff")
                        .staffRole(staff.getRole())
                        .build())
                .build();
    }

    @Override
    public void adminResetPassword(AdminResetPasswordDto dto) {
        Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, dto.getUsername()));
        if (staff == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "账号不存在");
        }
        if (staff.getStatus() == 0) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账号已被禁用");
        }
        if (staff.getPhone() == null || !staff.getPhone().equals(dto.getPhone())) {
            throw new BizException(ResultCode.BIZ_ERROR, "手机号与账号不匹配");
        }
        String redisCode = stringRedisTemplate.opsForValue().get(SMS_CODE_PREFIX + dto.getPhone());
        if (redisCode == null || !redisCode.equals(dto.getCode())) {
            throw new BizException(ResultCode.BIZ_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(SMS_CODE_PREFIX + dto.getPhone());

        // P0-3: 密码强度校验（≥8位，含大小写+数字）
        String pwd = dto.getNewPassword();
        if (pwd == null || pwd.length() < 8
                || !pwd.matches(".*[A-Z].*") || !pwd.matches(".*[a-z].*") || !pwd.matches(".*\\d.*")) {
            throw new BizException(ResultCode.PARAM_ERROR, "密码需≥8位且包含大小写字母和数字");
        }
        staff.setPasswordHash(passwordEncoder.encode(pwd));
        staffMapper.updateById(staff);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 1. 解析 refresh token
        Claims claims = jwtUtil.parse(refreshToken);

        // 2. 必须是 refresh 类型
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BizException(ResultCode.TOKEN_INVALID, "仅 refresh token 可用于续签");
        }

        // 3. 检查黑名单
        if (jwtBlacklistManager.isBlacklisted(refreshToken)) {
            throw new BizException(ResultCode.TOKEN_INVALID, "refresh token 已失效");
        }

        // 4. 旋转：将旧 refresh token 加入黑名单
        long remainMs = claims.getExpiration().getTime() - System.currentTimeMillis();
        jwtBlacklistManager.blacklist(refreshToken, remainMs);

        // 5. 签发新的 access + refresh
        String role = claims.get("role", String.class);
        String newAccessToken;
        String newRefreshToken;
        LocalDateTime accessExpireAt;
        LocalDateTime refreshExpireAt;

        if ("staff".equals(role)) {
            Long sid = claims.get("sid", Long.class);
            String staffRole = claims.get("staffRole", String.class);
            newAccessToken = jwtUtil.issueAdminAccessToken(sid, staffRole, "");
            newRefreshToken = jwtUtil.issueAdminRefreshToken(sid, staffRole);
            accessExpireAt = LocalDateTime.now().plusMinutes(jwtUtil.getAdminAccessTtlMinutes());
            refreshExpireAt = LocalDateTime.now().plusHours(jwtUtil.getAdminRefreshTtlHours());
            jwtBlacklistManager.registerActiveToken(newAccessToken, sid, (long) jwtUtil.getAdminAccessTtlMinutes() * 60_000);
        } else {
            Long uid = claims.get("uid", Long.class);
            newAccessToken = jwtUtil.issueUserAccessToken(uid, "");
            newRefreshToken = jwtUtil.issueUserRefreshToken(uid);
            accessExpireAt = LocalDateTime.now().plusMinutes(jwtUtil.getUserAccessTtlMinutes());
            refreshExpireAt = LocalDateTime.now().plusHours(jwtUtil.getUserRefreshTtlHours());
            jwtBlacklistManager.registerActiveToken(newAccessToken, uid, (long) jwtUtil.getUserAccessTtlMinutes() * 60_000);
        }

        return LoginVO.builder()
                .token(newAccessToken)
                .expireAt(accessExpireAt)
                .refreshToken(newRefreshToken)
                .refreshExpireAt(refreshExpireAt)
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
