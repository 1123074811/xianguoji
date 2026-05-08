package com.xianguoji.server.common.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 微信小程序工具类：调用 code2Session 获取 openid / session_key / unionid
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatUtil {

    @Value("${xianguoji.wechat.appid}")
    private String appid;

    @Value("${xianguoji.wechat.secret}")
    private String secret;

    private final StringRedisTemplate redisTemplate;

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    private static final String ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String GET_PHONE_URL =
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";
    private static final String REDIS_KEY_ACCESS_TOKEN = "wechat:access_token";

    // S-5: SSRF 白名单 —— 微信接口仅允许此前缀
    private static final Set<String> WECHAT_ALLOWED_PREFIXES = Set.of(
            "https://api.weixin.qq.com/"
    );

    /**
     * 调用微信 code2Session 接口
     *
     * @param jsCode 小程序调用 uni.login / wx.login 获得的 code
     * @return JSONObject 包含 openid / session_key / unionid(可选) / errcode / errmsg
     */
    public JSONObject code2Session(String jsCode) {
        String url = String.format(CODE2SESSION_URL, appid, secret, jsCode);

        // S-5: SSRF 防御 —— 校验 URL 白名单
        UrlSecurityUtil.validateUrlPrefix(url, WECHAT_ALLOWED_PREFIXES);

        String body = HttpUtil.get(url, 5000);
        log.debug("[WechatUtil] code2Session response: {}", body);

        JSONObject json = JSONUtil.parseObj(body);
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "unknown");
            log.error("[WechatUtil] code2Session failed: errcode={}, errmsg={}", errcode, errmsg);
            throw new BizException(ResultCode.THIRD_PARTY_ERROR, "微信登录失败: " + errmsg);
        }
        return json;
    }

    public String getAppid() {
        return appid;
    }

    /**
     * 获取微信 access_token（带 Redis 缓存，有效期 7200s，提前 5 分钟刷新）
     */
    public String getAccessToken() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY_ACCESS_TOKEN);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        String url = String.format(ACCESS_TOKEN_URL, appid, secret);
        UrlSecurityUtil.validateUrlPrefix(url, WECHAT_ALLOWED_PREFIXES);
        String body = HttpUtil.get(url, 5000);
        log.debug("[WechatUtil] getAccessToken response: {}", body);
        JSONObject json = JSONUtil.parseObj(body);
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "unknown");
            log.error("[WechatUtil] getAccessToken failed: errcode={}, errmsg={}", errcode, errmsg);
            throw new BizException(ResultCode.THIRD_PARTY_ERROR, "获取access_token失败: " + errmsg);
        }
        String accessToken = json.getStr("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BizException(ResultCode.THIRD_PARTY_ERROR, "获取access_token为空");
        }
        int expiresIn = json.getInt("expires_in", 7200);
        // 提前 300s 过期，避免边界问题
        redisTemplate.opsForValue().set(REDIS_KEY_ACCESS_TOKEN, accessToken, expiresIn - 300, TimeUnit.SECONDS);
        return accessToken;
    }

    /**
     * 使用微信新版 getPhoneNumber API，通过 code 直接换取手机号
     *
     * @param code getPhoneNumber 回调返回的 code
     * @return 纯手机号字符串（如 13800138000）
     */
    public String getPhoneNumber(String code) {
        String accessToken = getAccessToken();
        String url = String.format(GET_PHONE_URL, accessToken);
        UrlSecurityUtil.validateUrlPrefix(url, WECHAT_ALLOWED_PREFIXES);

        JSONObject reqBody = new JSONObject();
        reqBody.set("code", code);
        String resp = HttpUtil.post(url, reqBody.toString(), 5000);
        log.debug("[WechatUtil] getPhoneNumber response: {}", resp);

        JSONObject json = JSONUtil.parseObj(resp);
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "unknown");
            log.error("[WechatUtil] getPhoneNumber failed: errcode={}, errmsg={}", errcode, errmsg);
            // access_token 过期时清除缓存重试
            if (errcode == 42001 || errcode == 40001) {
                redisTemplate.delete(REDIS_KEY_ACCESS_TOKEN);
            }
            throw new BizException(ResultCode.THIRD_PARTY_ERROR, "获取手机号失败: " + errmsg);
        }

        JSONObject phoneInfo = json.getJSONObject("phone_info");
        if (phoneInfo == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "手机号信息为空");
        }
        String phone = phoneInfo.getStr("phoneNumber");
        if (phone == null || phone.isEmpty()) {
            throw new BizException(ResultCode.BIZ_ERROR, "未获取到手机号");
        }
        return phone;
    }
}
