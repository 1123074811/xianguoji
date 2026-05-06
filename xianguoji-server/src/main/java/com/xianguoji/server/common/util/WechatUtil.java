package com.xianguoji.server.common.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

/**
 * 微信小程序工具类：调用 code2Session 获取 openid / session_key / unionid
 */
@Slf4j
@Component
public class WechatUtil {

    @Value("${xianguoji.wechat.appid}")
    private String appid;

    @Value("${xianguoji.wechat.secret}")
    private String secret;

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

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
     * 解密微信小程序 getPhoneNumber 返回的加密数据，提取纯手机号
     *
     * @param sessionKey   code2Session 返回的 session_key
     * @param encryptedData getPhoneNumber 回调的 encryptedData
     * @param iv           getPhoneNumber 回调的 iv
     * @return 纯手机号字符串（如 13800138000）
     */
    public String decryptPhoneNumber(String sessionKey, String encryptedData, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] encBytes = Base64.getDecoder().decode(encryptedData);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new IvParameterSpec(ivBytes));
            byte[] plainBytes = cipher.doFinal(encBytes);
            String plainText = new String(plainBytes, StandardCharsets.UTF_8);

            JSONObject json = JSONUtil.parseObj(plainText);
            // 校验 appid 一致性，防篡改
            String watermarkAppid = json.getByPath("watermark.appid", String.class);
            if (watermarkAppid == null || !watermarkAppid.equals(appid)) {
                log.warn("[WechatUtil] decryptPhoneNumber watermark appid mismatch: expected={}, got={}", appid, watermarkAppid);
                throw new BizException(ResultCode.BIZ_ERROR, "手机号解密校验失败");
            }
            String phone = json.getStr("phoneNumber");
            if (phone == null || phone.isEmpty()) {
                throw new BizException(ResultCode.BIZ_ERROR, "未获取到手机号");
            }
            return phone;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("[WechatUtil] decryptPhoneNumber failed", e);
            throw new BizException(ResultCode.BIZ_ERROR, "手机号解密失败");
        }
    }
}
