package com.xianguoji.server.common.security;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Logback 脱敏编码器
 * - 手机号中间4位打码
 * - 身份证保留前6后4
 * - Bearer token 脱敏
 */
public class MaskingPatternEncoder extends PatternLayoutEncoder {

    private static final List<MaskRule> RULES = List.of(
            // 手机号：1[3-9]开头的11位数字，中间4位打码
            new MaskRule(Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})"), "$1****$2"),
            // 身份证：18位，保留前6后4
            new MaskRule(Pattern.compile("([1-9]\\d{5})\\d{8}(\\d{4})"), "$1********$2"),
            // Bearer token
            new MaskRule(Pattern.compile("Bearer\\s+\\S+"), "Bearer ***"),
            // Authorization header value
            new MaskRule(Pattern.compile("Authorization[=:]+\\s*\\S+"), "Authorization=***")
    );

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formatted = getLayout().doLayout(event);
        String masked = applyMasks(formatted);
        Charset cs = getCharset();
        if (cs == null) cs = StandardCharsets.UTF_8;
        return masked.getBytes(cs);
    }

    private String applyMasks(String text) {
        String result = text;
        for (MaskRule rule : RULES) {
            result = rule.pattern.matcher(result).replaceAll(rule.replacement);
        }
        return result;
    }

    private record MaskRule(Pattern pattern, String replacement) {}
}
