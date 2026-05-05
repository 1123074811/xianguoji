package com.xianguoji.server.module.staff.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.staff.dto.StaffAddDto;
import com.xianguoji.server.module.staff.dto.StaffUpdDto;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.staff.service.StaffService;
import com.xianguoji.server.module.staff.vo.StaffVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "员工管理")
@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@AdminRequired(roles = {"owner", "admin"})
public class StaffController {

    private final StaffService staffService;
    private final StaffMapper staffMapper;

    @Operation(summary = "当前员工安全信息")
    @GetMapping("/me/security")
    @AdminRequired
    public R<Map<String, Object>> meSecurity() {
        Staff me = staffMapper.selectById(LoginContext.sid());
        if (me == null) return R.fail(4040, "员工不存在");
        return R.ok(Map.of(
                "passwordChangedAt", me.getPasswordChangedAt() != null ? me.getPasswordChangedAt().toString() : "",
                "lastLoginAt", me.getLastLoginAt() != null ? me.getLastLoginAt().toString() : ""
        ));
    }

    @Operation(summary = "员工列表")
    @GetMapping("/page")
    public R<PageVO<StaffVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(staffService.staffPage(page, size));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    public R<Void> add(@Valid @RequestBody StaffAddDto dto) {
        staffService.addStaff(dto);
        return R.ok();
    }

    @Operation(summary = "更新员工")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody StaffUpdDto dto) {
        staffService.updateStaff(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        staffService.resetPassword(id, body.get("password"));
        return R.ok();
    }

    @Operation(summary = "改自己密码")
    @PutMapping("/me/password")
    @AdminRequired
    public R<Void> changeSelfPassword(@RequestBody Map<String, String> body) {
        staffService.changeSelfPassword(LoginContext.sid(), body.get("oldPassword"), body.get("newPassword"));
        return R.ok();
    }
}
