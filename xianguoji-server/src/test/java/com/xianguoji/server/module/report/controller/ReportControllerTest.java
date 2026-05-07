package com.xianguoji.server.module.report.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.module.report.entity.ReportExport;
import com.xianguoji.server.module.report.mapper.ReportExportMapper;
import com.xianguoji.server.module.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-REPORT series: Report controller API tests
 */
@DisplayName("Report Controller API Tests")
class ReportControllerTest extends AbstractApiTest {

    @MockBean
    private ReportService reportService;
    
    @MockBean
    private ReportExportMapper exportMapper;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("POST /api/admin/report/export")
    class ExportTests {

        @Test
        @DisplayName("TC-API-REPORT-001: Should export report successfully")
        void export_shouldExport_success() throws Exception {
            // Arrange
            ReportController.ExportDto dto = new ReportController.ExportDto();
            dto.reportType = "sales";
            dto.startDate = LocalDate.now().minusDays(7);
            dto.endDate = LocalDate.now();
            dto.format = "xlsx";
            
            byte[] testBytes = "test report data".getBytes();
            when(reportService.generate(anyString(), any(LocalDate.class), any(LocalDate.class), anyString())).thenReturn(testBytes);
            when(exportMapper.insert(any(ReportExport.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/admin/report/export", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            assertEquals("test report data".length(), result.getResponse().getContentLength());
            verify(reportService).generate(anyString(), any(LocalDate.class), any(LocalDate.class), anyString());
            verify(exportMapper).insert(any(ReportExport.class));
        }

        @Test
        @DisplayName("TC-API-REPORT-002: Should return error when end date before start date")
        void export_shouldReturnError_whenEndDateBeforeStartDate() throws Exception {
            // Arrange
            ReportController.ExportDto dto = new ReportController.ExportDto();
            dto.reportType = "sales";
            dto.startDate = LocalDate.now();
            dto.endDate = LocalDate.now().minusDays(1);
            dto.format = "xlsx";

            // Act
            MvcResult result = performPost("/api/admin/report/export", dto, adminToken)
                    .andReturn();

            // Assert
            assertEquals(400, result.getResponse().getStatus());
            assertTrue(result.getResponse().getContentAsString().contains("结束日期不能早于开始日期"));
        }

        @Test
        @DisplayName("TC-API-REPORT-003: Should return 403 when not admin")
        void export_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            ReportController.ExportDto dto = new ReportController.ExportDto();
            dto.reportType = "sales";
            dto.startDate = LocalDate.now().minusDays(7);
            dto.endDate = LocalDate.now();

            // Act
            MvcResult result = performPost("/api/admin/report/export", dto, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/report/list")
    class ListTests {

        @Test
        @DisplayName("TC-API-REPORT-004: Should return export history list")
        void list_shouldReturnList_success() throws Exception {
            // Arrange
            List<ReportExport> exports = new ArrayList<>();
            ReportExport export = new ReportExport();
            export.setId(1L);
            export.setReportType("sales");
            export.setFileName("sales_report.xlsx");
            export.setStatus(1);
            exports.add(export);
            
            when(exportMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(exports);

            // Act
            MvcResult result = performGet("/api/admin/report/list?limit=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(exportMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-REPORT-005: Should return 403 when not admin")
        void list_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/report/list", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/report/download/{id}")
    class DownloadHistoryTests {

        @Test
        @DisplayName("TC-API-REPORT-006: Should download history report successfully")
        void downloadHistory_shouldDownload_success() throws Exception {
            // Arrange
            Long id = 1L;
            ReportExport export = new ReportExport();
            export.setId(id);
            export.setStaffId(1L);
            export.setReportType("sales");
            export.setStartDate(LocalDate.now().minusDays(7));
            export.setEndDate(LocalDate.now());
            export.setFormat("xlsx");
            
            byte[] testBytes = "test report data".getBytes();
            when(exportMapper.selectById(id)).thenReturn(export);
            when(reportService.generate(anyString(), any(LocalDate.class), any(LocalDate.class), anyString())).thenReturn(testBytes);
            when(exportMapper.insert(any(ReportExport.class))).thenReturn(1);

            // Act
            MvcResult result = performGet("/api/admin/report/download/" + id, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            assertEquals("test report data".length(), result.getResponse().getContentLength());
        }

        @Test
        @DisplayName("TC-API-REPORT-007: Should return 404 when export not found")
        void downloadHistory_shouldReturn404_whenNotFound() throws Exception {
            // Arrange
            Long id = 999L;
            when(exportMapper.selectById(999L)).thenReturn(null);

            // Act
            MvcResult result = performGet("/api/admin/report/download/" + id, adminToken)
                    .andReturn();

            // Assert
            assertEquals(404, result.getResponse().getStatus());
        }

        @Test
        @DisplayName("TC-API-REPORT-008: Should return 403 when not admin")
        void downloadHistory_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/report/download/1", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/report/{id}")
    class DeleteTests {

        @Test
        @DisplayName("TC-API-REPORT-009: Should delete export record successfully")
        void delete_shouldDelete_success() throws Exception {
            // Arrange
            Long id = 1L;
            ReportExport export = new ReportExport();
            export.setId(id);
            export.setStaffId(1L);
            
            when(exportMapper.selectById(id)).thenReturn(export);
            when(exportMapper.deleteById(id)).thenReturn(1);

            // Act
            MvcResult result = performDelete("/api/admin/report/" + id, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(exportMapper).deleteById(id);
        }

        @Test
        @DisplayName("TC-API-REPORT-010: Should return ok when export not found")
        void delete_shouldReturnOk_whenNotFound() throws Exception {
            // Arrange
            Long id = 999L;
            when(exportMapper.selectById(999L)).thenReturn(null);

            // Act
            MvcResult result = performDelete("/api/admin/report/" + id, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(exportMapper, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("TC-API-REPORT-011: Should return 403 when not admin")
        void delete_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performDelete("/api/admin/report/1", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }
}

