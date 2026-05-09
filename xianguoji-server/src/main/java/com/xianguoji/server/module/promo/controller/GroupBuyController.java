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

    @Operation(summary = "查询商品的拼团活动")
    @GetMapping("/api/pub/group-buy/by-product/{productId}")
    public R<GroupBuyActivityVO> byProduct(@PathVariable Long productId) {
        return R.ok(groupBuyService.getActivityByProduct(productId));
    }

    @Operation(summary = "通过分享码查询拼团详情")
    @GetMapping("/api/pub/group-buy/share/{shareCode}")
    public R<GroupBuyInstanceVO> byShareCode(@PathVariable String shareCode) {
        return R.ok(groupBuyService.getInstanceByShareCode(shareCode));
    }

    @Operation(summary = "拼团实例详情（公开）")
    @GetMapping("/api/pub/group-buy/instance/{instanceId}")
    public R<GroupBuyInstanceVO> instanceDetail(@PathVariable Long instanceId) {
        return R.ok(groupBuyService.getInstanceDetail(instanceId));
    }

    @Operation(summary = "开团")
    @PostMapping("/api/u/group-buy/launch")
    @LoginRequired
    public R<Map<String, Object>> launch(@Valid @RequestBody GroupBuyLaunchDto dto) {
        Long instanceId = groupBuyService.launch(LoginContext.uid(), dto);
        GroupBuyInstanceVO vo = groupBuyService.getInstanceDetail(instanceId);
        return R.ok(Map.of("instanceId", instanceId, "shareCode", vo.getShareCode() == null ? "" : vo.getShareCode()));
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
