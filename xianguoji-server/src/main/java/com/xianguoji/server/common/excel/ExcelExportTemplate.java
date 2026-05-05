package com.xianguoji.server.common.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class ExcelExportTemplate {

    /**
     * 通用 Excel 导出模板
     *
     * @param response  HTTP 响应
     * @param fileName  下载文件名（不含扩展名）
     * @param sheetName Sheet 名称
     * @param headClazz Excel 行模型 class（需 @ExcelProperty 注解）
     * @param data      数据列表
     */
    public <T> void export(HttpServletResponse response, String fileName,
                           String sheetName, Class<T> headClazz, List<T> data) {
        try {
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), headClazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            log.error("Excel导出失败: fileName={}", fileName, e);
            throw new RuntimeException("导出失败", e);
        }
    }

    /**
     * 导出为 byte[]（用于异步导出 / OSS 上传等场景）
     */
    public <T> byte[] exportBytes(String sheetName, Class<T> headClazz, List<T> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, headClazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
        return out.toByteArray();
    }
}
