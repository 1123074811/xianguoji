package com.xianguoji.server.module.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.staff.dto.StaffAddDto;
import com.xianguoji.server.module.staff.dto.StaffUpdDto;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.staff.service.impl.StaffServiceImpl;
import com.xianguoji.server.module.staff.vo.StaffVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-STAFF series: Staff service unit tests
 */
@DisplayName("Staff Service Unit Tests")
class StaffServiceTest extends AbstractServiceTest {

    @Mock
    private StaffMapper staffMapper;

    @InjectMocks
    private StaffServiceImpl staffService;

    @Nested
    @DisplayName("staffPage")
    class StaffPageTests {

        @Test
        @DisplayName("TC-UT-STAFF-001: Should return staff page")
        void staffPage_shouldReturnPage_success() {
            // Arrange
            Staff staff = createStaff(1L, "admin", "Admin User", "1234567890");
            var page = new Page<Staff>(1, 20);
            page.setRecords(List.of(staff));
            page.setTotal(1);
            
            when(staffMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<StaffVO> result = staffService.staffPage(1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("TC-UT-STAFF-002: Should return empty page when no staff")
        void staffPage_shouldReturnEmptyPage_whenNoStaff() {
            // Arrange
            var page = new Page<Staff>(1, 20);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            
            when(staffMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<StaffVO> result = staffService.staffPage(1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotal());
        }
    }

    @Nested
    @DisplayName("addStaff")
    class AddStaffTests {

        @Test
        @DisplayName("TC-UT-STAFF-003: Should add staff successfully")
        void addStaff_shouldAdd_success() {
            // Arrange
            StaffAddDto dto = new StaffAddDto();
            dto.setUsername("newstaff");
            dto.setPassword("password123");
            dto.setName("New Staff");
            dto.setPhone("1234567890");
            dto.setRole("staff");
            
            when(staffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(staffMapper.insert(any(Staff.class))).thenReturn(1);

            // Act
            staffService.addStaff(dto);

            // Assert
            verify(staffMapper).insert(any(Staff.class));
        }

        @Test
        @DisplayName("TC-UT-STAFF-004: Should throw exception when username already exists")
        void addStaff_shouldThrowException_whenUsernameExists() {
            // Arrange
            StaffAddDto dto = new StaffAddDto();
            dto.setUsername("admin");
            dto.setPassword("password123");
            
            Staff existing = createStaff(1L, "admin", "Admin User", "1234567890");
            when(staffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                staffService.addStaff(dto);
            });
            assertEquals(ResultCode.CONFLICT, exception.getResultCode());
            assertTrue(exception.getMessage().contains("账号已存在"));
        }
    }

    @Nested
    @DisplayName("updateStaff")
    class UpdateStaffTests {

        @Test
        @DisplayName("TC-UT-STAFF-005: Should update staff successfully")
        void updateStaff_shouldUpdate_success() {
            // Arrange
            Long id = 1L;
            StaffUpdDto dto = new StaffUpdDto();
            dto.setName("Updated Name");
            
            Staff staff = createStaff(id, "admin", "Admin User", "1234567890");
            when(staffMapper.selectById(id)).thenReturn(staff);
            when(staffMapper.updateById(any(Staff.class))).thenReturn(1);

            // Act
            staffService.updateStaff(id, dto);

            // Assert
            verify(staffMapper).updateById(any(Staff.class));
        }

        @Test
        @DisplayName("TC-UT-STAFF-006: Should throw exception when staff not found")
        void updateStaff_shouldThrowException_whenStaffNotFound() {
            // Arrange
            Long id = 999L;
            StaffUpdDto dto = new StaffUpdDto();
            when(staffMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                staffService.updateStaff(id, dto);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }
    }

    @Nested
    @DisplayName("deleteStaff")
    class DeleteStaffTests {

        @Test
        @DisplayName("TC-UT-STAFF-007: Should delete staff successfully")
        void deleteStaff_shouldDelete_success() {
            // Arrange
            Long id = 1L;
            when(staffMapper.deleteById(id)).thenReturn(1);

            // Act
            staffService.deleteStaff(id);

            // Assert
            verify(staffMapper).deleteById(id);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("TC-UT-STAFF-008: Should reset password successfully")
        void resetPassword_shouldReset_success() {
            // Arrange
            Long id = 1L;
            String newPassword = "newPassword123";
            Staff staff = createStaff(id, "admin", "Admin User", "1234567890");
            
            when(staffMapper.selectById(id)).thenReturn(staff);
            when(staffMapper.updateById(any(Staff.class))).thenReturn(1);

            // Act
            staffService.resetPassword(id, newPassword);

            // Assert
            verify(staffMapper).updateById(any(Staff.class));
        }

        @Test
        @DisplayName("TC-UT-STAFF-009: Should throw exception when staff not found")
        void resetPassword_shouldThrowException_whenStaffNotFound() {
            // Arrange
            Long id = 999L;
            String newPassword = "newPassword123";
            when(staffMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                staffService.resetPassword(id, newPassword);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }
    }

    @Nested
    @DisplayName("changeSelfPassword")
    class ChangeSelfPasswordTests {

        @Test
        @DisplayName("TC-UT-STAFF-010: Should change self password successfully")
        void changeSelfPassword_shouldChange_success() {
            // Arrange
            Long sid = 1L;
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            Staff staff = createStaff(sid, "admin", "Admin User", "1234567890");
            staff.setPasswordHash("$2a$10$encodedHash");
            
            when(staffMapper.selectById(sid)).thenReturn(staff);
            when(staffMapper.updateById(any(Staff.class))).thenReturn(1);

            // Act
            staffService.changeSelfPassword(sid, oldPassword, newPassword);

            // Assert
            verify(staffMapper).updateById(any(Staff.class));
        }

        @Test
        @DisplayName("TC-UT-STAFF-011: Should throw exception when old password is incorrect")
        void changeSelfPassword_shouldThrowException_whenOldPasswordIncorrect() {
            // Arrange
            Long sid = 1L;
            String oldPassword = "wrongPassword";
            String newPassword = "newPassword";
            Staff staff = createStaff(sid, "admin", "Admin User", "1234567890");
            staff.setPasswordHash("$2a$10$encodedHash");
            
            when(staffMapper.selectById(sid)).thenReturn(staff);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                staffService.changeSelfPassword(sid, oldPassword, newPassword);
            });
            assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
            assertTrue(exception.getMessage().contains("原密码不正确"));
        }

        @Test
        @DisplayName("TC-UT-STAFF-012: Should throw exception when staff not found")
        void changeSelfPassword_shouldThrowException_whenStaffNotFound() {
            // Arrange
            Long sid = 999L;
            when(staffMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                staffService.changeSelfPassword(sid, "old", "new");
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }
    }

    // Helper methods
    private Staff createStaff(Long id, String username, String name, String phone) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setUsername(username);
        staff.setPasswordHash("$2a$10$encodedHash");
        staff.setName(name);
        staff.setPhone(phone);
        staff.setRole("staff");
        staff.setStatus(1);
        staff.setCreatedAt(LocalDateTime.now());
        return staff;
    }
}

