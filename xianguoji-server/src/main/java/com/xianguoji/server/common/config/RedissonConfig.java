package com.xianguoji.server.common.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnection;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 显式 Redisson 配置，避免 starter 自动配置在某些 Spring Boot 3.x 版本下失败
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonConfig {

    private String host = "127.0.0.1";
    private int port = 6379;
    private String password;
    private int database = 0;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(2);
        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }
        return Redisson.create(config);
    }
}
