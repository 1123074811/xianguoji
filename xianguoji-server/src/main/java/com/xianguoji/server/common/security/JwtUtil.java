package com.xianguoji.server.common.security;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * S-7: JWT 加固
 * - kid 轮换：签发用 active-kid，解析按 header kid 查密钥
 * - jti：每个 token 唯一 ID，替代全 token 作黑名单 key
 * - 双 token：access（短） + refresh（长），refresh 走 /api/auth/refresh
 * - 设备指纹：access token 绑定 fp = sha256(openid+ua).take(16)
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${xianguoji.jwt.active-kid:v1}")
    private String activeKid;

    @Value("${xianguoji.jwt.secrets.v1:CHANGE_ME_TO_32_BYTES_MIN}")
    private String secretV1;

    @Value("${xianguoji.jwt.secrets.v2:}")
    private String secretV2;

    @Value("${xianguoji.jwt.user-access-ttl-minutes:120}")
    private int userAccessTtlMinutes;

    @Value("${xianguoji.jwt.user-refresh-ttl-hours:168}")
    private int userRefreshTtlHours;

    @Value("${xianguoji.jwt.admin-access-ttl-minutes:30}")
    private int adminAccessTtlMinutes;

    @Value("${xianguoji.jwt.admin-refresh-ttl-hours:12}")
    private int adminRefreshTtlHours;

    // 兼容旧配置（单 secret）
    @Value("${xianguoji.jwt.secret:}")
    private String legacySecret;

    private SecretKey key(String kid) {
        String secret;
        if ("v2".equals(kid) && secretV2 != null && !secretV2.isBlank()) {
            secret = secretV2;
        } else if (secretV1 != null && !secretV1.isBlank() && !"CHANGE_ME_TO_32_BYTES_MIN".equals(secretV1)) {
            secret = secretV1;
        } else if (legacySecret != null && !legacySecret.isBlank() && !"CHANGE_ME_TO_32_BYTES_MIN".equals(legacySecret)) {
            secret = legacySecret;
        } else {
            throw new IllegalStateException("JWT secret 未配置，请设置 xianguoji.jwt.secrets 或 xianguoji.jwt.secret");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String getActiveKid() {
        if (secretV2 != null && !secretV2.isBlank()) {
            return activeKid;
        }
        return "v1";
    }

    // ---- Access Token 签发 ----

    public String issueUserAccessToken(Long uid, String fingerprint) {
        return issueAccessToken(String.valueOf(uid),
                Map.of("uid", uid, "role", "user", "type", "access", "fp", fingerprint != null ? fingerprint : ""),
                userAccessTtlMinutes);
    }

    public String issueAdminAccessToken(Long sid, String staffRole, String fingerprint) {
        return issueAccessToken(String.valueOf(sid),
                Map.of("sid", sid, "role", "staff", "staffRole", staffRole, "type", "access", "fp", fingerprint != null ? fingerprint : ""),
                adminAccessTtlMinutes);
    }

    // ---- Refresh Token 签发 ----

    public String issueUserRefreshToken(Long uid) {
        return issueRefreshToken(String.valueOf(uid),
                Map.of("uid", uid, "role", "user", "type", "refresh"),
                userRefreshTtlHours);
    }

    public String issueAdminRefreshToken(Long sid, String staffRole) {
        return issueRefreshToken(String.valueOf(sid),
                Map.of("sid", sid, "role", "staff", "staffRole", staffRole, "type", "refresh"),
                adminRefreshTtlHours);
    }

    // ---- 兼容旧接口（供过渡期使用） ----

    public String issueUserToken(Long uid) {
        return issueUserAccessToken(uid, "");
    }

    public String issueAdminToken(Long sid, String staffRole) {
        return issueAdminAccessToken(sid, staffRole, "");
    }

    // ---- 内部签发 ----

    private String issueAccessToken(String subject, Map<String, Object> claims, int ttlMinutes) {
        long now = System.currentTimeMillis();
        String kid = getActiveKid();
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .header().keyId(kid).and()
                .id(jti)
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + (long) ttlMinutes * 60_000))
                .signWith(key(kid))
                .compact();
    }

    private String issueRefreshToken(String subject, Map<String, Object> claims, int ttlHours) {
        long now = System.currentTimeMillis();
        String kid = getActiveKid();
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .header().keyId(kid).and()
                .id(jti)
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + (long) ttlHours * 3600_000))
                .signWith(key(kid))
                .compact();
    }

    // ---- 解析 ----

    public Claims parse(String token) {
        try {
            String kid = extractKid(token);
            return Jwts.parser()
                    .verifyWith(key(kid))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BizException(ResultCode.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
    }

    private String extractKid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "v1";
            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (headerJson.contains("\"kid\"")) {
                int idx = headerJson.indexOf("\"kid\"");
                int colon = headerJson.indexOf(':', idx);
                int start = headerJson.indexOf('"', colon + 1);
                int end = headerJson.indexOf('"', start + 1);
                return headerJson.substring(start + 1, end);
            }
        } catch (Exception ignored) {}
        return "v1";
    }

    /**
     * 生成设备指纹哈希：sha256(openidPrefix + ua).take(16)
     */
    public static String fingerprint(String openidPrefix, String userAgent) {
        String raw = (openidPrefix != null ? openidPrefix : "") + "|" + (userAgent != null ? userAgent : "");
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8 && i < hash.length; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public int getUserAccessTtlMinutes() { return userAccessTtlMinutes; }
    public int getUserRefreshTtlHours() { return userRefreshTtlHours; }
    public int getAdminAccessTtlMinutes() { return adminAccessTtlMinutes; }
    public int getAdminRefreshTtlHours() { return adminRefreshTtlHours; }
}
