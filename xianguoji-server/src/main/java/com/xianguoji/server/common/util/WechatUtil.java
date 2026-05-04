package com.xianguoji.server.common.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    /**
     * 调用微信 code2Session 接口
     *
     * @param jsCode 小程序调用 uni.login / wx.login 获得的 code
     * @return JSONObject 包含 openid / session_key / unionid(可选) / errcode / errmsg
     */
    public JSONObject code2Session(String jsCode) {
        String url = String.format(CODE2SESSION_URL, appid, secret, jsCode);
        String body = HttpUtil.get(url, 5000);
        log.debug("[WechatUtil] code2Session response: {}", body);

        JSONObject json = JSONUtil.parseObj(body);
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "unknown");
            log.error("[WechatUtil] code2Session failed: errcode={}, errmsg={}", errcode, errmsg);
            throw new RuntimeException("微信登录失败: " + errmsg);
        }
        return json;
    }

    public String getAppid() {
        return appid;
    }
}
