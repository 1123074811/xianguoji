package com.xianguoji.server.module.promo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GroupBuyInstanceVO {

    private Long id;
    private Long activityId;
    private Long leaderId;
    private String leaderName;
    private String leaderAvatar;
    private Integer currentSize;
    private Integer targetSize;
    private Integer status;
    private LocalDateTime expireAt;
    private List<ParticipantVO> participants;

    @Data
    @Builder
    public static class ParticipantVO {
        private Long userId;
        private String nickname;
        private String avatar;
        private Integer isLeader;
        private LocalDateTime joinedAt;
    }
}
