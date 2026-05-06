package com.xianguoji.server.common.validator;

import java.util.regex.Pattern;

/**
 * SQL 注入检测工具
 * - 检测常见注入特征
 * - 清洗 LIKE 通配符
 */
public final class SqlInjectionValidator {

    private SqlInjectionValidator() {}

    /** 匹配常见 SQL 注入关键字与模式 */
    private static final Pattern SQLI_PATTERN = Pattern.compile(
            "(?i)\\b(SELECT|INSERT|UPDATE|DELETE|DROP|TRUNCATE|ALTER|EXEC|EXECUTE|UNION|DECLARE|CREATE|GRANT|REVOKE)\\b"
            + "|(?i)\\bOR\\s+1\\s*=\\s*1"
            + "|(?i)\\bAND\\s+1\\s*=\\s*1"
            + "|--"
            + "|/\\*"
            + "\\*/"
            + "|;\\s*(DROP|DELETE|TRUNCATE|ALTER|GRANT|REVOKE)\\b"
            + "|(?i)\\bXP_\\w+"
            + "|(?i)\\bINFORMATION_SCHEMA\\b"
            + "|(?i)\\bWAITFOR\\s+DELAY\\b"
            + "|(?i)\\bBENCHMARK\\s*\\("
            + "|(?i)\\bSLEEP\\s*\\("
    );

    /**
     * 检测字符串是否包含 SQL 注入特征
     * @return true 表示检测到注入特征
     */
    public static boolean containsSqlInjection(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return SQLI_PATTERN.matcher(value).find();
    }

    /**
     * 转义 LIKE 通配符 % _ \，防止 LIKE 风暴
     */
    public static String cleanLikeWildcard(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
