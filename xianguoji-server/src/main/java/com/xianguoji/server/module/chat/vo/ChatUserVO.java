package com.xianguoji.server.module.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserVO {

    private Long userId;
    private String nickname;
    private String avatar;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}
