package com.xianguoji.server.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StaffMapper staffMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Override
    public void run(String... args) {
        initAdminAccount();
    }

    private void initAdminAccount() {
        Staff existing = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, "admin"));
        if (existing != null) {
            // 确保 admin 密码为 "admin" 的哈希
            existing.setPasswordHash(passwordEncoder.encode("admin"));
            staffMapper.updateById(existing);
            log.info("admin 账号已存在，已重置密码为 admin");
            return;
        }

        Staff admin = new Staff();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setName("超级管理员");
        admin.setPhone("13800000000");
        admin.setRole("owner");
        admin.setPermissions(List.of("*"));
        admin.setStatus(1);
        staffMapper.insert(admin);

        log.info("============================================");
        log.info("  已自动创建初始管理员账号");
        log.info("  商家端登录 → 用户名: admin  密码: admin");
        log.info("============================================");
    }
}
