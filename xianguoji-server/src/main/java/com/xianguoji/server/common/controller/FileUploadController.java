package com.xianguoji.server.common.controller;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.util.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final OssService ossService;

    private static final long MAX_SIZE = 100L * 1024 * 1024; // 100MB
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("jpg", "jpeg", "png", "webp");

    @PostMapping("/api/pub/file/upload-avatar")
    public R<String> publicUploadAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file, "avatar"));
    }

    @PostMapping("/api/u/file/upload")
    public R<String> userUpload(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file, "user"));
    }

    @PostMapping("/api/admin/file/upload")
    public R<String> adminUpload(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file, "admin"));
    }

    private String upload(MultipartFile file, String dir) {
        if (file.isEmpty()) throw new BizException(ResultCode.PARAM_ERROR, "文件不能为空");
        if (file.getSize() > MAX_SIZE) throw new BizException(ResultCode.PARAM_ERROR, "文件不能超过100MB");

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BizException(ResultCode.PARAM_ERROR, "仅支持jpg/jpeg/png/webp格式");
        }

        return ossService.upload(file, dir, ext, file.getContentType());
    }
}
