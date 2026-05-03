package com.xianguoji.server.module.message.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.message.dto.FeedbackAddDto;
import com.xianguoji.server.module.message.service.MessageService;
import com.xianguoji.server.module.message.vo.FeedbackVO;
import com.xianguoji.server.module.message.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "消息/反馈")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "分页消息")
    @GetMapping("/api/u/message/page")
    @LoginRequired
    public R<PageVO<MessageVO>> messagePage(@RequestParam(required = false) Integer type,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(messageService.getMessagePage(LoginContext.uid(), type, page, size));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/api/u/message/{id}/read")
    @LoginRequired
    public R<Void> markRead(@PathVariable Long id) {
        messageService.markRead(LoginContext.uid(), id);
        return R.ok();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/api/u/message/read-all")
    @LoginRequired
    public R<Void> markAllRead() {
        messageService.markAllRead(LoginContext.uid());
        return R.ok();
    }

    @Operation(summary = "未读数")
    @GetMapping("/api/u/message/unread-count")
    @LoginRequired
    public R<Integer> unreadCount() {
        return R.ok(messageService.getUnreadCount(LoginContext.uid()));
    }

    @Operation(summary = "提交反馈")
    @PostMapping("/api/u/feedback")
    @LoginRequired
    public R<Void> submitFeedback(@Valid @RequestBody FeedbackAddDto dto) {
        messageService.submitFeedback(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "反馈列表")
    @GetMapping("/api/admin/feedback/page")
    @AdminRequired
    public R<PageVO<FeedbackVO>> feedbackPage(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(messageService.getFeedbackPage(page, size));
    }

    @Operation(summary = "标记已处理")
    @PutMapping("/api/admin/feedback/{id}/handle")
    @AdminRequired
    public R<Void> handleFeedback(@PathVariable Long id) {
        messageService.handleFeedback(id);
        return R.ok();
    }

    @Operation(summary = "发广播消息")
    @PostMapping("/api/admin/message/broadcast")
    @AdminRequired
    public R<Void> broadcast(@RequestBody Map<String, String> body) {
        messageService.broadcast(body.get("title"), body.get("content"), body.get("linkUrl"));
        return R.ok();
    }
}
