package com.xianguoji.server.module.help.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.help.entity.HelpFaq;
import com.xianguoji.server.module.help.entity.HelpFeedback;
import com.xianguoji.server.module.help.entity.HelpGuide;
import com.xianguoji.server.module.help.mapper.HelpFaqMapper;
import com.xianguoji.server.module.help.mapper.HelpFeedbackMapper;
import com.xianguoji.server.module.help.mapper.HelpGuideMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "帮助中心")
@RestController
@RequestMapping("/api/admin/help")
@RequiredArgsConstructor
@AdminRequired
public class HelpController {

    private final HelpFaqMapper faqMapper;
    private final HelpGuideMapper guideMapper;
    private final HelpFeedbackMapper feedbackMapper;

    @Operation(summary = "FAQ 列表")
    @GetMapping("/faq/list")
    public R<List<HelpFaq>> faqList(@RequestParam(required = false) String section,
                                    @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<HelpFaq> w = new LambdaQueryWrapper<HelpFaq>()
                .eq(HelpFaq::getStatus, 1)
                .orderByAsc(HelpFaq::getSort);
        if (section != null && !section.isBlank()) w.eq(HelpFaq::getSection, section);
        if (keyword != null && !keyword.isBlank()) {
            w.and(c -> c.like(HelpFaq::getQuestion, keyword).or().like(HelpFaq::getAnswer, keyword));
        }
        return R.ok(faqMapper.selectList(w));
    }

    @Operation(summary = "操作指南列表")
    @GetMapping("/guide/list")
    public R<List<HelpGuide>> guideList() {
        return R.ok(guideMapper.selectList(new LambdaQueryWrapper<HelpGuide>().orderByAsc(HelpGuide::getSort)));
    }

    @Operation(summary = "提交反馈")
    @PostMapping("/feedback")
    public R<Void> submitFeedback(@RequestBody FeedbackDto dto) {
        if (dto.content == null || dto.content.isBlank()) {
            return R.fail(4001, "反馈内容不能为空");
        }
        HelpFeedback fb = new HelpFeedback();
        fb.setStaffId(LoginContext.sid());
        fb.setContent(dto.content);
        fb.setContact(dto.contact);
        fb.setStatus(0);
        feedbackMapper.insert(fb);
        return R.ok();
    }

    @Data
    public static class FeedbackDto {
        @NotBlank @Size(max = 1000) public String content;
        @Size(max = 64) public String contact;
    }
}
