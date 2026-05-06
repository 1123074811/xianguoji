package com.xianguoji.server.common.service;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * S-17: 安全事件告警消费者
 * - 从 Redis Stream (xgj:sec:events) 消费事件
 * - 高危事件推送钉钉/飞书 webhook
 */
@Slf4j
@Component
public class SecurityEventAlerter {

    private final StringRedisTemplate redis;
    private final String webhookUrl;
    private final boolean streamAvailable;

    private static final String STREAM = "xgj:sec:events";
    private static final String GROUP = "sec-alerter";
    private static final Pattern REDIS_VER = Pattern.compile("redis_version:(\\d+)\\.(\\d+)");

    public SecurityEventAlerter(StringRedisTemplate redis,
                                @Value("${xianguoji.security.alert-webhook:}") String webhookUrl) {
        this.redis = redis;
        this.webhookUrl = webhookUrl;
        this.streamAvailable = checkStreamSupport();
        if (!streamAvailable) {
            log.warn("[S-17] Redis < 5.0，Stream 不可用，安全事件告警已禁用");
            return;
        }
        // 创建消费者组（幂等）
        try {
            redis.opsForStream().createGroup(STREAM, GROUP);
        } catch (Exception ignored) {}
    }

    private boolean checkStreamSupport() {
        try {
            String info = redis.execute((org.springframework.data.redis.core.RedisCallback<String>) con -> {
                Object result = con.serverCommands().info("server");
                return result == null ? "" : result.toString();
            });
            Matcher m = REDIS_VER.matcher(info);
            if (m.find()) {
                int major = Integer.parseInt(m.group(1));
                int minor = Integer.parseInt(m.group(2));
                return major > 5 || (major == 5 && minor >= 0);
            }
        } catch (Exception e) {
            log.warn("[S-17] 无法检测 Redis 版本: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 每 10 秒拉取未处理事件
     */
    @Scheduled(fixedDelay = 10_000, initialDelay = 30_000)
    public void poll() {
        if (!streamAvailable) return;
        try {
            List<MapRecord<String, Object, Object>> records =
                    redis.opsForStream().read(Consumer.from(GROUP, "alerter"),
                            StreamOffset.create(STREAM, ReadOffset.lastConsumed()));
            if (records == null || records.isEmpty()) return;

            for (MapRecord<String, Object, Object> record : records) {
                Map<Object, Object> body = record.getValue();
                String type = String.valueOf(body.getOrDefault("type", "UNKNOWN"));
                String detail = String.valueOf(body.getOrDefault("detail", ""));

                // 高危事件推送 webhook
                if (isHighRisk(type)) {
                    sendAlert(type, detail);
                }
                // ACK
                redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());
            }
        } catch (Exception e) {
            log.error("[S-17] poll error: {}", e.getMessage());
        }
    }

    private boolean isHighRisk(String type) {
        return switch (type) {
            case "IDOR_ATTEMPT", "UPLOAD_MISMATCH", "BRUTE_FORCE", "TOKEN_TAMPER",
                 "SSRF_BLOCKED", "IP_BANNED" -> true;
            default -> false;
        };
    }

    private void sendAlert(String type, String detail) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("[S-17] 无告警webhook配置，跳过推送: type={}", type);
            return;
        }
        try {
            String payload = """
                    {"msgtype":"text","text":{"content":"[鲜果记安全告警] %s - %s"}}
                    """.formatted(type, detail);
            HttpUtil.post(webhookUrl, payload, 5000);
            log.info("[S-17] 告警已推送: type={}", type);
        } catch (Exception e) {
            log.error("[S-17] webhook推送失败: {}", e.getMessage());
        }
    }
}
