package com.xianguoji.server.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsUtil {

    @Value("${xianguoji.sms.provider:mock}")
    private String provider;

    public void send(String phone, String code) {
        if ("mock".equals(provider)) {
            log.info("[SMS Mock] phone={}, code={}", phone, code);
        } else {
            throw new UnsupportedOperationException("TODO: 集成阿里云短信 SDK");
        }
    }
}
