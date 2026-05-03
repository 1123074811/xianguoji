package com.xianguoji.server.module.user.controller;

import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.user.dto.AddressAddDto;
import com.xianguoji.server.module.user.dto.AddressUpdDto;
import com.xianguoji.server.module.user.dto.UserProfileUpdDto;
import com.xianguoji.server.module.user.service.UserService;
import com.xianguoji.server.module.user.vo.AddressVO;
import com.xianguoji.server.module.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户中心")
@RestController
@RequestMapping("/api/u")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "我的资料")
    @GetMapping("/user/profile")
    @LoginRequired
    public R<UserProfileVO> profile() {
        return R.ok(userService.getProfile(LoginContext.uid()));
    }

    @Operation(summary = "修改资料")
    @PutMapping("/user/profile")
    @LoginRequired
    public R<Void> updateProfile(@Valid @RequestBody UserProfileUpdDto dto) {
        userService.updateProfile(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "地址列表")
    @GetMapping("/address/list")
    @LoginRequired
    public R<List<AddressVO>> addressList() {
        return R.ok(userService.addressList(LoginContext.uid()));
    }

    @Operation(summary = "新增地址")
    @PostMapping("/address")
    @LoginRequired
    public R<Void> addAddress(@Valid @RequestBody AddressAddDto dto) {
        userService.addAddress(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "更新地址")
    @PutMapping("/address/{id}")
    @LoginRequired
    public R<Void> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressUpdDto dto) {
        userService.updateAddress(LoginContext.uid(), id, dto);
        return R.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/address/{id}")
    @LoginRequired
    public R<Void> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(LoginContext.uid(), id);
        return R.ok();
    }

    @Operation(summary = "设默认地址")
    @PutMapping("/address/{id}/default")
    @LoginRequired
    public R<Void> setDefaultAddress(@PathVariable Long id) {
        userService.setDefaultAddress(LoginContext.uid(), id);
        return R.ok();
    }
}
