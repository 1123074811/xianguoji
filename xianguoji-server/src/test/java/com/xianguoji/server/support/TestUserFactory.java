package com.xianguoji.server.support;

import com.xianguoji.server.common.security.JwtUtil;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Factory for creating test users and generating JWT tokens.
 */
@Component
public class TestUserFactory {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TestUserFactory(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create a test user and return the user ID.
     */
    public Long createTestUser(String phone, String nickname) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setAvatar(null);
        user.setGender(0);
        user.setBirthday(null);
        user.setTag(null);
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * Generate a JWT token for a user ID.
     */
    public String generateUserToken(Long userId) {
        return jwtUtil.issueUserAccessToken(userId, "");
    }

    /**
     * Create a test user and return the token.
     */
    public String createTestUserWithToken(String phone, String nickname) {
        Long userId = createTestUser(phone, nickname);
        return generateUserToken(userId);
    }

    /**
     * Clean up test user by ID.
     */
    public void cleanupUser(Long userId) {
        if (userId != null) {
            userMapper.deleteById(userId);
        }
    }
}
