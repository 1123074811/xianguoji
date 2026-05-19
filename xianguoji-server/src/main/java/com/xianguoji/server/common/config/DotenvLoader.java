package com.xianguoji.server.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 不依赖 IDEA 工作目录加载 .env：依次探测多个候选路径，找到即用。
 * 通过 META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports 注册。
 * prod 环境下校验关键 secret 是否存在，缺失则阻止启动。
 */
public class DotenvLoader implements EnvironmentPostProcessor {

    private static final String[] CANDIDATES = {
            ".env",
            "xianguoji-server/.env",
            "../xianguoji-server/.env",
            "../.env"
    };

    private static final List<String> PROD_REQUIRED_SECRETS = List.of(
            "MYSQL_PASSWORD",
            "REDIS_PASSWORD",
            "WECHAT_APPID",
            "WECHAT_SECRET"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        for (String c : CANDIDATES) {
            Path p = Paths.get(c).toAbsolutePath().normalize();
            if (Files.isRegularFile(p)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(p)) {
                    props.load(in);
                } catch (Exception e) {
                    System.err.println("[DotenvLoader] 读取失败: " + p + " -> " + e.getMessage());
                    return;
                }
                env.getPropertySources().addFirst(new PropertiesPropertySource("dotenv-file", props));
                System.out.println("[DotenvLoader] 已加载: " + p);
                break;
            }
        }

        // prod 环境校验关键 secret
        String[] activeProfiles = env.getActiveProfiles();
        boolean isProd = Arrays.asList(activeProfiles).contains("prod");
        if (isProd) {
            for (String key : PROD_REQUIRED_SECRETS) {
                requireNonBlank(env, key);
            }
            String activeKid = firstNonBlank(env.getProperty("JWT_ACTIVE_KID"), env.getProperty("xianguoji.jwt.active-kid"), "v1");
            String kidSecret = firstNonBlank(env.getProperty("JWT_SECRET_" + activeKid.toUpperCase()), env.getProperty("xianguoji.jwt.secrets." + activeKid));
            String legacySecret = firstNonBlank(env.getProperty("JWT_SECRET"), env.getProperty("xianguoji.jwt.secret"));
            String jwtSecret = firstNonBlank(kidSecret, legacySecret);
            if (jwtSecret == null || jwtSecret.length() < 32) {
                throw new IllegalStateException("[DotenvLoader] 生产环境 JWT 密钥缺失或长度不足，启动终止");
            }
            String corsOrigins = firstNonBlank(env.getProperty("CORS_ALLOWED_ORIGINS"), env.getProperty("xianguoji.security.cors.allowed-origins"));
            if (corsOrigins == null || corsOrigins.isBlank() || corsOrigins.contains("*")) {
                throw new IllegalStateException("[DotenvLoader] 生产环境 CORS_ALLOWED_ORIGINS 不能为空且不能包含 *，启动终止");
            }
            if (isTrue(env.getProperty("springdoc.api-docs.enabled")) || isTrue(env.getProperty("springdoc.swagger-ui.enabled")) || isTrue(env.getProperty("knife4j.enable"))) {
                throw new IllegalStateException("[DotenvLoader] 生产环境禁止开启 Swagger/Knife4j，启动终止");
            }
            String uploadMax = firstNonBlank(env.getProperty("UPLOAD_MAX_SIZE_MB"), env.getProperty("xianguoji.security.upload-max-size-mb"), env.getProperty("xianguoji.upload.max-size-mb"));
            if (uploadMax != null) {
                try {
                    if (Integer.parseInt(uploadMax) > 10) {
                        throw new IllegalStateException("[DotenvLoader] 生产环境上传大小不能超过 10MB，启动终止");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("[DotenvLoader] 生产环境上传大小配置非法，启动终止");
                }
            }
            System.out.println("[DotenvLoader] prod 环境关键 secret 校验通过");
        }
    }

    private static void requireNonBlank(ConfigurableEnvironment env, String key) {
        String value = env.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("[DotenvLoader] 生产环境缺少必要配置: " + key + "，启动终止");
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static boolean isTrue(String value) {
        return "true".equalsIgnoreCase(value);
    }
}
