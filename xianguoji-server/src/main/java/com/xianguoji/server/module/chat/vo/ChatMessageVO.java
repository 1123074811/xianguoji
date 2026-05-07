package com.xianguoji.server.module.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {

    private Long id;
    /** 0=用户发送 1=商家回复 */
    private Integer senderType;
    /** text / product / image */
    private String msgType;
    private String content;
    /** 商品卡片信息 */
    private ProductCardVO productCard;
    /** 图片URL列表 */
    private List<String> images;
    private Integer isRead;
    /** 发送者头像URL */
    private String senderAvatar;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCardVO {
        private Long productId;
        private String name;
        private String mainImage;
        private String price;
        private String specName;
    }
}
