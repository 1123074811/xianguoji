package com.xianguoji.server.module.chat.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.chat.dto.ChatSendDto;
import com.xianguoji.server.module.chat.service.ChatService;
import com.xianguoji.server.module.chat.vo.ChatMessageVO;
import com.xianguoji.server.module.chat.vo.ChatUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "在线客服")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息")
    @PostMapping("/api/u/chat/send")
    @LoginRequired
    public R<ChatMessageVO> sendMessage(@Valid @RequestBody ChatSendDto dto) {
        return R.ok(chatService.sendMessage(LoginContext.uid(), dto));
    }

    @Operation(summary = "聊天记录")
    @GetMapping("/api/u/chat/messages")
    @LoginRequired
    public R<PageVO<ChatMessageVO>> messagePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(chatService.getMessagePage(LoginContext.uid(), page, size));
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/api/u/chat/unread-count")
    @LoginRequired
    public R<Integer> unreadCount() {
        return R.ok(chatService.getUnreadCount(LoginContext.uid()));
    }

    @Operation(summary = "全部已读")
    @PutMapping("/api/u/chat/read-all")
    @LoginRequired
    public R<Void> markAllRead() {
        chatService.markAllRead(LoginContext.uid());
        return R.ok();
    }

    // ---- 管理端 ----

    @Operation(summary = "聊天用户列表")
    @GetMapping("/api/admin/chat/users")
    @AdminRequired
    public R<PageVO<ChatUserVO>> chatUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(chatService.getChatUserPage(page, size));
    }

    @Operation(summary = "管理端-聊天记录")
    @GetMapping("/api/admin/chat/messages")
    @AdminRequired
    public R<PageVO<ChatMessageVO>> adminMessagePage(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(chatService.getAdminMessagePage(userId, page, size));
    }

    @Operation(summary = "管理端-回复消息")
    @PostMapping("/api/admin/chat/reply")
    @AdminRequired
    public R<ChatMessageVO> replyMessage(@RequestParam Long userId,
                                          @Valid @RequestBody ChatSendDto dto) {
        return R.ok(chatService.replyMessage(userId, dto));
    }
}
