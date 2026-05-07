package com.xianguoji.server.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.message.dto.FeedbackAddDto;
import com.xianguoji.server.module.message.entity.Feedback;
import com.xianguoji.server.module.message.entity.Message;
import com.xianguoji.server.module.message.mapper.FeedbackMapper;
import com.xianguoji.server.module.message.mapper.MessageMapper;
import com.xianguoji.server.module.message.service.MessageService;
import com.xianguoji.server.module.message.vo.FeedbackVO;
import com.xianguoji.server.module.message.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final FeedbackMapper feedbackMapper;

    @Override
    public PageVO<MessageVO> getMessagePage(Long uid, Integer type, Integer page, Integer size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getUserId, uid).or().eq(Message::getUserId, 0));
        applyTypeFilter(wrapper, type);
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> p = messageMapper.selectPage(new Page<>(page, size), wrapper);
        List<MessageVO> voList = p.getRecords().stream().map(m -> MessageVO.builder()
                .id(m.getId()).type(m.getType()).title(m.getTitle())
                .content(m.getContent()).linkUrl(m.getLinkUrl())
                .isRead(m.getIsRead()).createdAt(m.getCreatedAt()).build()).collect(Collectors.toList());
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    public MessageVO getMessage(Long uid, Long id) {
        Message msg = messageMapper.selectOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getId, id)
                .and(w -> w.eq(Message::getUserId, uid).or().eq(Message::getUserId, 0))
                .last("LIMIT 1"));
        if (msg == null) throw new BizException(ResultCode.NOT_FOUND);
        return MessageVO.builder()
                .id(msg.getId())
                .type(msg.getType())
                .title(msg.getTitle())
                .content(msg.getContent())
                .linkUrl(msg.getLinkUrl())
                .isRead(msg.getIsRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    @Override
    public void markRead(Long uid, Long id) {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, id)
                .and(w -> w.eq(Message::getUserId, uid).or().eq(Message::getUserId, 0))
                .set(Message::getIsRead, 1));
    }

    @Override
    public void markAllRead(Long uid, Integer type) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<Message>()
                .and(w -> w.eq(Message::getUserId, uid).or().eq(Message::getUserId, 0));
        applyTypeFilter(wrapper, type);
        wrapper.set(Message::getIsRead, 1);
        messageMapper.update(null, wrapper);
    }

    @Override
    public int getUnreadCount(Long uid) {
        return Math.toIntExact(messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getIsRead, 0)
                        .and(w -> w.eq(Message::getUserId, uid).or().eq(Message::getUserId, 0))));
    }

    @Override
    public Map<String, Integer> getUnreadCounts(Long uid) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("system", countUnreadByType(uid, 1));
        counts.put("promotion", countUnreadByType(uid, 3));
        counts.put("logistics", countUnreadByType(uid, 2));
        counts.put("chat", countUnreadByType(uid, 6));
        counts.put("all", getUnreadCount(uid));
        return counts;
    }

    @Override
    public void submitFeedback(Long uid, FeedbackAddDto dto) {
        Feedback fb = new Feedback();
        fb.setUserId(uid);
        fb.setType(dto.getType());
        fb.setContent(dto.getContent());
        fb.setImages(dto.getImages());
        fb.setContact(dto.getContact());
        fb.setStatus(0);
        feedbackMapper.insert(fb);
    }

    @Override
    public PageVO<FeedbackVO> getFeedbackPage(Integer page, Integer size) {
        Page<Feedback> p = feedbackMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Feedback>().orderByDesc(Feedback::getCreatedAt));
        List<FeedbackVO> voList = p.getRecords().stream().map(f -> FeedbackVO.builder()
                .id(f.getId()).userId(f.getUserId()).type(f.getType())
                .content(f.getContent()).images(f.getImages()).contact(f.getContact())
                .status(f.getStatus()).createdAt(f.getCreatedAt()).build()).collect(Collectors.toList());
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    public void handleFeedback(Long id) {
        Feedback fb = feedbackMapper.selectById(id);
        if (fb == null) throw new BizException(ResultCode.NOT_FOUND);
        fb.setStatus(1);
        feedbackMapper.updateById(fb);
    }

    @Override
    public void broadcast(String title, String content, String linkUrl) {
        Message msg = new Message();
        msg.setUserId(0L);
        msg.setType(1);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setLinkUrl(linkUrl);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    private int countUnreadByType(Long uid, Integer type) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getIsRead, 0)
                .and(w -> w.eq(Message::getUserId, uid).or().eq(Message::getUserId, 0));
        applyTypeFilter(wrapper, type);
        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }

    private void applyTypeFilter(LambdaQueryWrapper<Message> wrapper, Integer type) {
        if (type == null) return;
        if (type == 2) {
            wrapper.in(Message::getType, 2, 4);
        } else {
            wrapper.eq(Message::getType, type);
        }
    }

    private void applyTypeFilter(LambdaUpdateWrapper<Message> wrapper, Integer type) {
        if (type == null) return;
        if (type == 2) {
            wrapper.in(Message::getType, 2, 4);
        } else {
            wrapper.eq(Message::getType, type);
        }
    }
}
