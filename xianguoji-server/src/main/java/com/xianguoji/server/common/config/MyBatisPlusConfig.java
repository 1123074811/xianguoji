package com.xianguoji.server.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.common.security.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig implements MetaObjectHandler {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        Long operatorId = getCurrentOperatorId();
        if (operatorId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, operatorId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, operatorId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        Long operatorId = getCurrentOperatorId();
        if (operatorId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, operatorId);
        }
    }

    private Long getCurrentOperatorId() {
        LoginUser loginUser = LoginContext.get();
        if (loginUser == null) return null;
        // 用户端取 uid，商家端取 sid
        return loginUser.getUid() != null ? loginUser.getUid() : loginUser.getSid();
    }
}
