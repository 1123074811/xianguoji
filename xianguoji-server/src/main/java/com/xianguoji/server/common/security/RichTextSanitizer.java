package com.xianguoji.server.common.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 富文本白名单清洗器
 * 用于商品描述、公告等允许保留 HTML 标签的字段
 */
public final class RichTextSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addAttributes(":all", "style", "class")
            .removeProtocols("a", "href", "ftp", "file")
            .removeProtocols("img", "src", "ftp", "file");

    private RichTextSanitizer() {}

    /**
     * 清洗 HTML，保留白名单标签，剥离 script / 事件属性 / javascript: 协议
     */
    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        return Jsoup.clean(html, "", SAFELIST,
                new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
    }
}
