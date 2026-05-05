package com.xianguoji.server.module.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.RateLimit;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.JwtBlacklistManager;
import com.xianguoji.server.common.security.JwtUtil;
import com.xianguoji.server.common.util.SmsUtil;
import com.xianguoji.server.common.util.WechatUtil;
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
    public void sendSmsCode(SmsSendDto dto) {
        String phone = dto.getPhone();

        // 60秒内不可重复发送
        String limitKey = SMS_LIMIT_PREFIX + phone + ":sec";
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(limitKey))) {
            throw new BizException(ResultCode.RATE_LIMITED, "发送太频繁，请60秒后重试");
        }

        // 每日上限
        String dayKey = SMS_LIMIT_PREFIX + phone + ":day";
        Long count = stringRedisTemplate.opsForValue().increment(dayKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(dayKey, 1, TimeUnit.DAYS);
        }
        if (count != null && count > DAILY_LIMIT) {
            throw new BizException(ResultCode.RATE_LIMITED, "今日发送次数已达上限");
        }

        String code = "mock".equals(smsProvider) ? DEV_FIXED_CODE : RandomUtil.randomNumbers(4);
        stringRedisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);

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

        String token = jwtUtil.issueUserToken(user.getId());
        LocalDateTime expireAt = LocalDateTime.now().plusHours(168);

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(token, user.getId(), 168 * 3600_000L);

        return LoginVO.builder()
                .token(token)
                .expireAt(expireAt)
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

        String token = jwtUtil.issueUserToken(user.getId());
        LocalDateTime expireAt = LocalDateTime.now().plusHours(168);

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(token, user.getId(), 168 * 3600_000L);

        return LoginVO.builder()
                .token(token)
                .expireAt(expireAt)
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

        // 4. 签发 JWT
        String token = jwtUtil.issueUserToken(user.getId());
        LocalDateTime expireAt = LocalDateTime.now().plusHours(168);

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(token, user.getId(), 168 * 3600_000L);

        return LoginVO.builder()
                .token(token)
                .expireAt(expireAt)
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
    public LoginVO adminLogin(AdminLoginDto dto) {
        // 校验图形验证码
        String redisCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + dto.getCaptchaKey());
        if (redisCode == null || !redisCode.equals(dto.getCaptchaCode().toLowerCase())) {
            throw new BizException(ResultCode.BIZ_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + dto.getCaptchaKey());

        Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, dto.getUsername()));
        if (staff == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "账号或密码错误");
        }
        if (staff.getStatus() == 0) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), staff.getPasswordHash())) {
            throw new BizException(ResultCode.BIZ_ERROR, "账号或密码错误");
        }

        staff.setLastLoginAt(LocalDateTime.now());
        staffMapper.updateById(staff);

        String token = jwtUtil.issueAdminToken(staff.getId(), staff.getRole());
        LocalDateTime expireAt = LocalDateTime.now().plusHours(12);

        // P2-8: 注册活跃 token
        jwtBlacklistManager.registerActiveToken(token, staff.getId(), 12 * 3600_000L);

        return LoginVO.builder()
                .token(token)
                .expireAt(expireAt)
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

        staff.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        staffMapper.updateById(staff);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
