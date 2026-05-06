package com.xianguoji.server.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * P1-3: Redis 哨兵配置
 * 生产环境切换为哨兵模式时，在 application-prod.yml 中配置 sentinel 节点
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xianguoji.redis-sentinel")
public class RedisSentinelProperties {

    private boolean enabled = false;

    /** 哨兵 master 名称 */
    private String master = "mymaster";

    /** 哨兵节点列表，如 "127.0.0.1:26379,127.0.0.1:26380,127.0.0.1:26381" */
    private String nodes = "";

    /** 哨兵密码 */
    private String password;
}
