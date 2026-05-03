package com.xianguoji.server.module.promo.controller;

import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.promo.dto.GroupBuyLaunchDto;
import com.xianguoji.server.module.promo.dto.GroupBuyJoinDto;
import com.xianguoji.server.module.promo.service.GroupBuyService;
import com.xianguoji.server.module.promo.vo.GroupBuyActivityVO;
import com.xianguoji.server.module.promo.vo.GroupBuyInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "拼团")
@RestController
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

    @Operation(summary = "拼团活动列表")
    @GetMapping("/api/pub/group-buy/page")
    public R<PageVO<GroupBuyActivityVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(groupBuyService.getGroupBuyPage(page, size));
    }

    @Operation(summary = "开团")
    @PostMapping("/api/u/group-buy/launch")
    @LoginRequired
    public R<Map<String, Long>> launch(@Valid @RequestBody GroupBuyLaunchDto dto) {
        Long instanceId = groupBuyService.launch(LoginContext.uid(), dto);
        return R.ok(Map.of("instanceId", instanceId));
    }

    @Operation(summary = "参团")
    @PostMapping("/api/u/group-buy/{instanceId}/join")
    @LoginRequired
    public R<Void> join(@PathVariable Long instanceId, @Valid @RequestBody GroupBuyJoinDto dto) {
        groupBuyService.join(LoginContext.uid(), instanceId, dto);
        return R.ok();
    }

    @Operation(summary = "拼团详情")
    @GetMapping("/api/u/group-buy/{instanceId}")
    @LoginRequired
    public R<GroupBuyInstanceVO> detail(@PathVariable Long instanceId) {
        return R.ok(groupBuyService.getInstanceDetail(instanceId));
    }
}
