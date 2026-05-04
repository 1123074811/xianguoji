package com.xianguoji.server.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("report_export")
public class ReportExport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long staffId;
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String format;
    private String fileName;
    private Long fileSize;
    private Integer status;
    private LocalDateTime createdAt;
}
