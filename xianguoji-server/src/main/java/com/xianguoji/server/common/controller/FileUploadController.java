package com.xianguoji.server.common.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.LoginRequired;
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

    private static final long USER_MAX_SIZE = 5L * 1024 * 1024;
    private static final long ADMIN_MAX_SIZE = 10L * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final java.util.Set<String> ALLOWED_MIME = java.util.Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @PostMapping("/api/pub/file/upload-avatar")
    public R<String> publicUploadAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file, "avatar", USER_MAX_SIZE));
    }

    @PostMapping("/api/u/file/upload")
    @LoginRequired
    public R<String> userUpload(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file, "user", USER_MAX_SIZE));
    }

    @PostMapping("/api/admin/file/upload")
    @AdminRequired
    public R<String> adminUpload(@RequestParam("file") MultipartFile file) {
        return R.ok(upload(file, "admin", ADMIN_MAX_SIZE));
    }

    private String upload(MultipartFile file, String dir, long maxSize) {
        if (file.isEmpty()) throw new BizException(ResultCode.PARAM_ERROR, "文件不能为空");
        if (file.getSize() > maxSize) throw new BizException(ResultCode.PARAM_ERROR, "文件不能超过" + (maxSize / 1024 / 1024) + "MB");

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BizException(ResultCode.PARAM_ERROR, "仅支持jpg/jpeg/png/webp/gif格式");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME.contains(contentType.toLowerCase())) {
            throw new BizException(ResultCode.PARAM_ERROR, "不支持的文件类型");
        }

        return ossService.upload(file, dir, ext, contentType);
    }
}
