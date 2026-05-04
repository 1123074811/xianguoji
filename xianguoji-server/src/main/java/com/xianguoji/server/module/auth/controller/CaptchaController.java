package com.xianguoji.server.module.auth.controller;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.xianguoji.server.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "验证码")
@RestController
@RequiredArgsConstructor
public class CaptchaController {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TTL_MINUTES = 5;

    @Operation(summary = "获取图形验证码")
    @GetMapping(value = "/api/pub/captcha", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<Map<String, String>> captcha() {
        LineCaptcha lineCaptcha = new LineCaptcha(120, 40, 4, 6);
        String code = lineCaptcha.getCode();
        String key = IdUtil.fastSimpleUUID();

        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code.toLowerCase(), CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

        String imageBase64 = lineCaptcha.getImageBase64Data();

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", imageBase64);
        return R.ok(result);
    }
}
