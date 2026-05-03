package com.xianguoji.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xianguoji.server.module.*.mapper")
@EnableScheduling
public class XianguojiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XianguojiApplication.class, args);
    }
}
