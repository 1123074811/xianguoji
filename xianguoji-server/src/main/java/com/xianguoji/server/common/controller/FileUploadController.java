package com.xianguoji.server.common.controller;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
public class FileUploadController {

    @Value("${xianguoji.upload.base-dir:D:/xianguoji/upload}")
    private String baseDir;

    @Value("${xianguoji.upload.domain:http://127.0.0.1:8080/static}")
    private String domain;

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("jpg", "jpeg", "png", "webp");

    @PostMapping("/api/pub/file/upload-avatar")
    public R<String> publicUploadAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file));
    }

    @PostMapping("/api/u/file/upload")
    public R<String> userUpload(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file));
    }

    @PostMapping("/api/admin/file/upload")
    public R<String> adminUpload(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file));
    }

    private String upload(MultipartFile file) {
        if (file.isEmpty()) throw new BizException(ResultCode.PARAM_ERROR, "文件不能为空");
        if (file.getSize() > MAX_SIZE) throw new BizException(ResultCode.PARAM_ERROR, "文件不能超过5MB");

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BizException(ResultCode.PARAM_ERROR, "仅支持jpg/jpeg/png/webp格式");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            Path dir = Paths.get(baseDir, datePath);
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        return domain + "/" + datePath + "/" + fileName;
    }
}
