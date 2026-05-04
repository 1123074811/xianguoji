package com.xianguoji.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
@MapperScan("com.xianguoji.server.module.*.mapper")
@EnableScheduling
public class XianguojiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XianguojiApplication.class, args);
    }
}
