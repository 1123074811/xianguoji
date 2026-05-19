package com.xianguoji.server.module.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.report.entity.ReportExport;
import com.xianguoji.server.module.report.mapper.ReportExportMapper;
import com.xianguoji.server.module.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "报表导出")
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
@AdminRequired(roles = {"owner", "admin", "finance"})
public class ReportController {

    private final ReportService reportService;
    private final ReportExportMapper exportMapper;

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Operation(summary = "导出报表")
    @PostMapping("/export")
    public void export(@RequestBody ExportDto dto, HttpServletResponse response) throws Exception {
        String type = dto.reportType == null ? "sales" : dto.reportType;
        String format = dto.format == null ? "xlsx" : dto.format.toLowerCase();
        LocalDate start = dto.startDate;
        LocalDate end = dto.endDate;
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();
        if (end.isBefore(start)) {
            response.setStatus(400);
            response.getWriter().write("结束日期不能早于开始日期");
            return;
        }

        byte[] bytes = reportService.generate(type, start, end, format);
        String typeName = nameOf(type);
        String fileName = String.format("%s_%s_%s.%s", typeName, start.format(F), end.format(F),
                "csv".equals(format) ? "csv" : "xlsx");

        // 写入历史记录
        ReportExport rec = new ReportExport();
        rec.setStaffId(LoginContext.sid());
        rec.setReportType(type);
        rec.setStartDate(start);
        rec.setEndDate(end);
        rec.setFormat(format);
        rec.setFileName(fileName);
        rec.setFileSize((long) bytes.length);
        rec.setStatus(1);
        exportMapper.insert(rec);

        response.setContentType("csv".equals(format)
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    @Operation(summary = "历史记录")
    @GetMapping("/list")
    public R<List<ReportExport>> list(@RequestParam(defaultValue = "20") int limit) {
        Long sid = LoginContext.sid();
        List<ReportExport> list = exportMapper.selectList(new LambdaQueryWrapper<ReportExport>()
                .eq(ReportExport::getStaffId, sid)
                .orderByDesc(ReportExport::getCreatedAt)
                .last("LIMIT " + Math.min(Math.max(limit, 1), 100)));
        return R.ok(list);
    }

    @Operation(summary = "下载历史报表（按记录重新生成）")
    @GetMapping("/download/{id}")
    public void downloadHistory(@PathVariable Long id, HttpServletResponse response) throws Exception {
        ReportExport rec = exportMapper.selectById(id);
        if (rec == null || !rec.getStaffId().equals(LoginContext.sid())) {
            response.setStatus(404);
            return;
        }
        ExportDto dto = new ExportDto();
        dto.reportType = rec.getReportType();
        dto.startDate = rec.getStartDate();
        dto.endDate = rec.getEndDate();
        dto.format = rec.getFormat();
        export(dto, response);
    }

    @Operation(summary = "删除历史记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        ReportExport rec = exportMapper.selectById(id);
        if (rec != null && rec.getStaffId().equals(LoginContext.sid())) {
            exportMapper.deleteById(id);
        }
        return R.ok();
    }

    private String nameOf(String type) {
        return switch (type) {
            case "sales" -> "销售报表";
            case "inventory" -> "库存报表";
            case "customer" -> "客户报表";
            case "delivery" -> "配送报表";
            case "finance" -> "财务报表";
            default -> "报表";
        };
    }

    @Data
    public static class ExportDto {
        @NotBlank
        public String reportType;
        @NotNull
        public LocalDate startDate;
        @NotNull
        public LocalDate endDate;
        public String format;
        public Boolean includeCharts;
        public Boolean detailedMode;
    }
}
