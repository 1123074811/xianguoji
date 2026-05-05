package com.xianguoji.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
        com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
@MapperScan("com.xianguoji.server.module.*.mapper")
@EnableScheduling
@EnableAsync
public class XianguojiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XianguojiApplication.class, args);
    }
}
