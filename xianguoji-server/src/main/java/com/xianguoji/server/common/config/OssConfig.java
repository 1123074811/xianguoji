package com.xianguoji.server.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${xianguoji.oss.access-key-id:}') " +
        "&& !T(org.springframework.util.StringUtils).isEmpty('${xianguoji.oss.access-key-secret:}') " +
        "&& !T(org.springframework.util.StringUtils).isEmpty('${xianguoji.oss.endpoint:}') " +
        "&& !T(org.springframework.util.StringUtils).isEmpty('${xianguoji.oss.bucket:}')"
)
public class OssConfig {

    private final OssProperties props;

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder().build(
                props.getEndpoint(),
                props.getAccessKeyId(),
                props.getAccessKeySecret()
        );
    }
}
