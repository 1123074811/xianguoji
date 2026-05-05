package com.xianguoji.server.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.xianguoji.server.common.config.OssProperties;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnBean(OSS.class)
@RequiredArgsConstructor
public class OssService {

    private final OSS ossClient;
    private final OssProperties props;

    public String upload(MultipartFile file, String dir, String ext, String contentType) {
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

    private String buildUrl(String key) {
        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            return props.getDomain().replaceAll("/+$", "") + "/" + key;
        }
        return "https://" + props.getBucket() + "." + props.getEndpoint() + "/" + key;
    }
}
