package com.xianguoji.server.support;

import com.xianguoji.server.common.security.JwtUtil;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory for creating test staff accounts and generating JWT tokens.
 */
@Component
public class TestStaffFactory {

    private final StaffMapper staffMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TestStaffFactory(StaffMapper staffMapper, JwtUtil jwtUtil) {
        this.staffMapper = staffMapper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create a test staff account and return the staff ID.
     */
    public Long createTestStaff(String username, String role, Long shopId) {
        Staff staff = new Staff();
        staff.setUsername(username);
        staff.setPasswordHash(passwordEncoder.encode("password123"));
        staff.setName("Test Staff");
        staff.setPhone("13800138000");
        staff.setRole(role); // OWNER, MANAGER, CLERK
        staff.setPermissions(List.of("ORDER_READ", "ORDER_WRITE"));
        staff.setStatus(1); // active
        staff.setCreatedAt(LocalDateTime.now());
        staffMapper.insert(staff);
        return staff.getId();
    }

    /**
     * Generate a JWT token for a staff ID.
     */
    public String generateStaffToken(Long staffId, String role) {
        return jwtUtil.issueAdminAccessToken(staffId, role, "");
    }

    /**
     * Create a test staff and return the token.
     */
    public String createTestStaffWithToken(String username, String role, Long shopId) {
        Long staffId = createTestStaff(username, role, shopId);
        return generateStaffToken(staffId, role);
    }

    /**
     * Clean up test staff by ID.
     */
    public void cleanupStaff(Long staffId) {
        if (staffId != null) {
            staffMapper.deleteById(staffId);
        }
    }
}
