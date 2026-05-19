package com.xianguoji.server.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.xianguoji.server.common.config.OssProperties;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.service.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnBean(OSS.class)
@RequiredArgsConstructor
public class OssService {

    private final OSS ossClient;
    private final OssProperties props;
    private final SecurityEventService securityEventService;

    // S-10: 允许的文件扩展名白名单
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    // S-10: 允许的 MIME 类型白名单
    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    // S-10: magic number 前缀映射（十六进制）
    private static final Map<String, String> MAGIC_NUMBERS = Map.of(
            "jpg",  "FFD8FF",
            "jpeg", "FFD8FF",
            "png",  "89504E47",
            "gif",  "47494638",
            "webp", "52494646"
    );

    @Value("${xianguoji.upload.max-size-mb:10}")
    private int maxSizeMb;

    public String upload(MultipartFile file, String dir, String ext, String contentType) {
        // S-10: 扩展名白名单校验
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            log.warn("[S-10] 不支持的文件扩展名: ext={}", ext);
            throw new BizException(ResultCode.PARAM_ERROR, "不支持的文件类型");
        }

        // S-10: MIME 类型校验
        if (contentType != null && !ALLOWED_MIME.contains(contentType.toLowerCase())) {
            log.warn("[S-10] 不支持的MIME类型: contentType={}", contentType);
            throw new BizException(ResultCode.PARAM_ERROR, "不支持的文件类型");
        }

        // S-10: 文件大小校验
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BizException(ResultCode.PARAM_ERROR, "文件大小超出限制（最大" + maxSizeMb + "MB）");
        }

        // S-10: magic number 校验（真实文件类型）
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = in.read(header);
            if (read < 4) {
                throw new BizException(ResultCode.PARAM_ERROR, "文件内容异常");
            }
            String hexHeader = bytesToHex(header).toUpperCase();
            String expectedMagic = MAGIC_NUMBERS.get(ext.toLowerCase());
            if (expectedMagic != null && !hexHeader.startsWith(expectedMagic)) {
                log.warn("[S-10] 文件magic number不匹配: ext={}, header={}", ext, hexHeader.substring(0, Math.min(16, hexHeader.length())));
                securityEventService.log("UPLOAD_MISMATCH", "", "ext=" + ext + " header=" + hexHeader.substring(0, 16));
                throw new BizException(ResultCode.PARAM_ERROR, "文件内容与扩展名不匹配");
            }
            if ("webp".equalsIgnoreCase(ext) && !hexHeader.substring(16, Math.min(24, hexHeader.length())).equals("57454250")) {
                log.warn("[S-10] WebP文件格式不匹配: header={}", hexHeader.substring(0, Math.min(24, hexHeader.length())));
                securityEventService.log("UPLOAD_MISMATCH", "", "ext=" + ext + " header=" + hexHeader.substring(0, 16));
                throw new BizException(ResultCode.PARAM_ERROR, "文件内容与扩展名不匹配");
            }
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件校验失败");
        }

        // S-10: 文件名一律用 UUID，绝不使用用户传入名（防路径穿越和 XSS）
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String key = (dir == null || dir.isBlank() ? "upload" : dir.replaceAll("^/+|/+$", ""))
                + "/" + datePath + "/" + fileName;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        if (contentType != null) meta.setContentType(contentType);

        try (InputStream in = file.getInputStream()) {
            ossClient.putObject(props.getBucket(), key, in, meta);
        } catch (IOException e) {
            log.error("OSS上传失败", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
        return buildUrl(key);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private String buildUrl(String key) {
        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            return props.getDomain().replaceAll("/+$", "") + "/" + key;
        }
        return "https://" + props.getBucket() + "." + props.getEndpoint() + "/" + key;
    }
}
