package com.xianguoji.server.module.staff.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.staff.dto.StaffAddDto;
import com.xianguoji.server.module.staff.dto.StaffUpdDto;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.staff.service.StaffService;
import com.xianguoji.server.module.staff.vo.StaffVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-STAFF series: Staff controller API tests
 */
@DisplayName("Staff Controller API Tests")
class StaffControllerTest extends AbstractApiTest {

    @MockBean
    private StaffService staffService;
    
    @MockBean
    private StaffMapper staffMapper;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/admin/staff/me/security")
    class MeSecurityTests {

        @Test
        @DisplayName("TC-API-STAFF-001: Should return staff security info")
        void meSecurity_shouldReturnSecurityInfo_success() throws Exception {
            // Arrange
            Staff staff = createStaff(1L, "admin", "Admin User", "1234567890");
            staff.setPasswordChangedAt(LocalDateTime.now());
            staff.setLastLoginAt(LocalDateTime.now());
            
            when(staffMapper.selectById(1L)).thenReturn(staff);

            // Act
            MvcResult result = performGet("/api/admin/staff/me/security", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffMapper).selectById(anyLong());
        }

        @Test
        @DisplayName("TC-API-STAFF-002: Should return 403 when not admin")
        void meSecurity_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/staff/me/security", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/staff/page")
    class StaffPageTests {

        @Test
        @DisplayName("TC-API-STAFF-003: Should return staff page")
        void page_shouldReturnPage_success() throws Exception {
            // Arrange
            List<StaffVO> staffList = new ArrayList<>();
            StaffVO staff = StaffVO.builder()
                    .id(1L)
                    .username("admin")
                    .name("Admin User")
                    .role("admin")
                    .status(1)
                    .build();
            staffList.add(staff);
            
            PageVO<StaffVO> pageVO = new PageVO<>(1L, staffList, 1, 20);
            when(staffService.staffPage(1, 20)).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/admin/staff/page?page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffService).staffPage(1, 20);
        }

        @Test
        @DisplayName("TC-API-STAFF-004: Should return 403 when not admin")
        void page_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/staff/page", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/staff")
    class AddStaffTests {

        @Test
        @DisplayName("TC-API-STAFF-005: Should add staff successfully")
        void add_shouldAdd_success() throws Exception {
            // Arrange
            StaffAddDto dto = new StaffAddDto();
            dto.setUsername("newstaff");
            dto.setPassword("password123");
            dto.setName("New Staff");
            dto.setPhone("1234567890");
            dto.setRole("staff");
            
            doNothing().when(staffService).addStaff(any(StaffAddDto.class));

            // Act
            MvcResult result = performPost("/api/admin/staff", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffService).addStaff(any(StaffAddDto.class));
        }

        @Test
        @DisplayName("TC-API-STAFF-006: Should return 403 when not admin")
        void add_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            StaffAddDto dto = new StaffAddDto();

            // Act
            MvcResult result = performPost("/api/admin/staff", dto, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/staff/{id}")
    class UpdateStaffTests {

        @Test
        @DisplayName("TC-API-STAFF-007: Should update staff successfully")
        void update_shouldUpdate_success() throws Exception {
            // Arrange
            Long id = 1L;
            StaffUpdDto dto = new StaffUpdDto();
            dto.setName("Updated Name");
            
            doNothing().when(staffService).updateStaff(eq(id), any(StaffUpdDto.class));

            // Act
            MvcResult result = performPut("/api/admin/staff/" + id, dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffService).updateStaff(eq(id), any(StaffUpdDto.class));
        }

        @Test
        @DisplayName("TC-API-STAFF-008: Should return 403 when not admin")
        void update_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            StaffUpdDto dto = new StaffUpdDto();

            // Act
            MvcResult result = performPut("/api/admin/staff/1", dto, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/staff/{id}")
    class DeleteStaffTests {

        @Test
        @DisplayName("TC-API-STAFF-009: Should delete staff successfully")
        void delete_shouldDelete_success() throws Exception {
            // Arrange
            Long id = 1L;
            doNothing().when(staffService).deleteStaff(id);

            // Act
            MvcResult result = performDelete("/api/admin/staff/" + id, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffService).deleteStaff(id);
        }

        @Test
        @DisplayName("TC-API-STAFF-010: Should return 403 when not admin")
        void delete_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performDelete("/api/admin/staff/1", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/staff/{id}/password")
    class ResetPasswordTests {

        @Test
        @DisplayName("TC-API-STAFF-011: Should reset password successfully")
        void resetPassword_shouldReset_success() throws Exception {
            // Arrange
            Long id = 1L;
            Map<String, String> body = Map.of("password", "newPassword123");
            doNothing().when(staffService).resetPassword(eq(id), anyString());

            // Act
            MvcResult result = performPut("/api/admin/staff/" + id + "/password", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffService).resetPassword(eq(id), anyString());
        }

        @Test
        @DisplayName("TC-API-STAFF-012: Should return 403 when not admin")
        void resetPassword_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Map<String, String> body = Map.of("password", "newPassword");

            // Act
            MvcResult result = performPut("/api/admin/staff/1/password", body, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/staff/me/password")
    class ChangeSelfPasswordTests {

        @Test
        @DisplayName("TC-API-STAFF-013: Should change self password successfully")
        void changeSelfPassword_shouldChange_success() throws Exception {
            // Arrange
            Map<String, String> body = Map.of(
                    "oldPassword", "oldPassword",
                    "newPassword", "newPassword"
            );
            doNothing().when(staffService).changeSelfPassword(anyLong(), anyString(), anyString());

            // Act
            MvcResult result = performPut("/api/admin/staff/me/password", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(staffService).changeSelfPassword(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("TC-API-STAFF-014: Should return 403 when not admin")
        void changeSelfPassword_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Map<String, String> body = Map.of("oldPassword", "old", "newPassword", "new");

            // Act
            MvcResult result = performPut("/api/admin/staff/me/password", body, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    // Helper methods
    private Staff createStaff(Long id, String username, String name, String phone) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setUsername(username);
        staff.setName(name);
        staff.setPhone(phone);
        staff.setRole("admin");
        staff.setStatus(1);
        return staff;
    }
}

