package com.xianguoji.server.module.message.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.message.dto.FeedbackAddDto;
import com.xianguoji.server.module.message.vo.FeedbackVO;
import com.xianguoji.server.module.message.vo.MessageVO;

import java.util.Map;

public interface MessageService {

    PageVO<MessageVO> getMessagePage(Long uid, Integer type, Integer page, Integer size);

    MessageVO getMessage(Long uid, Long id);

    void markRead(Long uid, Long id);

    void markAllRead(Long uid, Integer type);

    int getUnreadCount(Long uid);

    Map<String, Integer> getUnreadCounts(Long uid);

    void submitFeedback(Long uid, FeedbackAddDto dto);

    PageVO<FeedbackVO> getFeedbackPage(Integer page, Integer size);

    void handleFeedback(Long id);

    void broadcast(String title, String content, String linkUrl);
}
