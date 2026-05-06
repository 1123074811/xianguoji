package com.xianguoji.server.common.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * XSS 请求包装器
 * - getParameter / getParameterValues / getHeader: 对纯文本做 HTML 实体编码
 * - getInputStream / getReader: 对 JSON 请求体递归清洗 String 值
 * - 缓存 body 为 byte[]，避免下游再次读取流时为空
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 缓存的请求体 */
    private byte[] cachedBody;

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    // ==================== 参数 & Header 转义 ====================

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return cleanXss(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = cleanXss(values[i]);
        }
        return cleaned;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        map.forEach((key, values) -> {
            for (int i = 0; i < values.length; i++) {
                values[i] = cleanXss(values[i]);
            }
        });
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        // 不转义 Authorization / Content-Type 等技术头
        if ("authorization".equalsIgnoreCase(name)
                || "content-type".equalsIgnoreCase(name)
                || "accept".equalsIgnoreCase(name)) {
            return value;
        }
        return cleanXss(value);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        // 不转义技术头
        if ("authorization".equalsIgnoreCase(name)
                || "content-type".equalsIgnoreCase(name)
                || "accept".equalsIgnoreCase(name)) {
            return super.getHeaders(name);
        }
        return Collections.enumeration(
                Collections.list(super.getHeaders(name)).stream()
                        .map(XssHttpServletRequestWrapper::cleanXss)
                        .toList());
    }

    // ==================== Body 清洗 ====================

    /**
     * 读取并缓存 body，若是 JSON 则递归清洗 String 值
     */
    private byte[] getCachedBody() throws IOException {
        if (cachedBody != null) {
            return cachedBody;
        }
        byte[] raw = super.getInputStream().readAllBytes();
        String contentType = getContentType();

        if (contentType != null && contentType.contains("application/json")) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(raw);
                JsonNode cleaned = cleanJsonNode(root);
                cachedBody = OBJECT_MAPPER.writeValueAsBytes(cleaned);
            } catch (Exception e) {
                // JSON 解析失败，直接转义原始文本
                cachedBody = cleanXss(new String(raw, StandardCharsets.UTF_8))
                        .getBytes(StandardCharsets.UTF_8);
            }
        } else {
            // 非 JSON body 不做清洗（multipart 等由 XssFilter 排除，不会走到这里）
            cachedBody = raw;
        }
        return cachedBody;
    }

    /**
     * 递归清洗 JSON 节点中的 String 值
     */
    private JsonNode cleanJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            ObjectNode newObj = OBJECT_MAPPER.createObjectNode();
            obj.fields().forEachRemaining(entry -> {
                if (entry.getValue().isTextual()) {
                    newObj.put(entry.getKey(), cleanXss(entry.getValue().asText()));
                } else {
                    newObj.set(entry.getKey(), cleanJsonNode(entry.getValue()));
                }
            });
            return newObj;
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            ArrayNode newArr = OBJECT_MAPPER.createArrayNode();
            for (JsonNode element : arr) {
                newArr.add(cleanJsonNode(element));
            }
            return newArr;
        }
        return node;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        byte[] body = getCachedBody();
        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() { return bais.available() == 0; }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException(); }
            @Override
            public int read() { return bais.read(); }
            @Override
            public int read(byte[] b, int off, int len) { return bais.read(b, off, len); }
        };
    }

    // ==================== XSS 清洗核心 ====================

    /**
     * 对普通文本做 HTML 实体编码，防止 XSS
     * 转义 < > "（防 HTML 标签注入与属性闭合）
     *
     * 注意：故意不转义 & ' /，原因：
     * - / 编码会破坏 URL（imageUrl/avatar）、日期 "2024/01/01"、地址中的 "/"
     * - & 编码会与 querystring/已转义文本叠加导致双重转义
     * - ' 在 JSON / SQL 参数化场景下不构成 XSS 风险
     * 真正危险的是 HTML 标签注入，转义 < > " 即可阻断 90% 以上 XSS 向量；
     * 富文本内容仍由 RichTextSanitizer (Jsoup) 二次清洗。
     */
    public static String cleanXss(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        // 快速路径：无危险字符则直接返回原值，避免对 URL/JSON 数据无谓改写
        boolean dirty = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '<' || c == '>' || c == '"') { dirty = true; break; }
        }
        if (!dirty) return value;

        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
