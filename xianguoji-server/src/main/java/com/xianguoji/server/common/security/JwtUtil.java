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

@Slf4j
@Component
public class JwtUtil {

    @Value("${xianguoji.jwt.secret}")
    private String secret;

    @Value("${xianguoji.jwt.user-ttl-hours}")
    private int userTtlHours;

    @Value("${xianguoji.jwt.admin-ttl-hours}")
    private int adminTtlHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(String subject, Map<String, Object> claims, int ttlHours) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + (long) ttlHours * 3600_000))
                .signWith(key())
                .compact();
    }

    public String issueUserToken(Long uid) {
        return issue(String.valueOf(uid),
                Map.of("uid", uid, "role", "user"),
                userTtlHours);
    }

    public String issueAdminToken(Long sid, String staffRole) {
        return issue(String.valueOf(sid),
                Map.of("sid", sid, "role", "staff", "staffRole", staffRole),
                adminTtlHours);
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BizException(ResultCode.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
    }
}
