package com.xianguoji.server.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 不依赖 IDEA 工作目录加载 .env：依次探测多个候选路径，找到即用。
 * 通过 META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports 注册。
 */
public class DotenvLoader implements EnvironmentPostProcessor {

    private static final String[] CANDIDATES = {
            ".env",
            "xianguoji-server/.env",
            "../xianguoji-server/.env",
            "../.env"
    };

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
                return;
            }
        }
        System.out.println("[DotenvLoader] 未找到 .env，跳过");
    }
}
