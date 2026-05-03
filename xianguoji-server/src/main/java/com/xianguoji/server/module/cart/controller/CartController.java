package com.xianguoji.server.module.cart.controller;

import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.cart.dto.CartAddDto;
import com.xianguoji.server.module.cart.dto.CartQuantityUpdDto;
import com.xianguoji.server.module.cart.dto.CartSelectedDto;
import com.xianguoji.server.module.cart.service.CartService;
import com.xianguoji.server.module.cart.vo.CartListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "购物车")
@RestController
@RequestMapping("/api/u/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping("/list")
    @LoginRequired
    public R<CartListVO> list() {
        return R.ok(cartService.getCartList(LoginContext.uid()));
    }

    @Operation(summary = "加车")
    @PostMapping
    @LoginRequired
    public R<Void> add(@Valid @RequestBody CartAddDto dto) {
        cartService.addToCart(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "改数量")
    @PutMapping("/{id}/quantity")
    @LoginRequired
    public R<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartQuantityUpdDto dto) {
        cartService.updateQuantity(LoginContext.uid(), id, dto.getQuantity());
        return R.ok();
    }

    @Operation(summary = "批量勾选")
    @PutMapping("/selected")
    @LoginRequired
    public R<Void> updateSelected(@Valid @RequestBody CartSelectedDto dto) {
        cartService.updateSelected(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @LoginRequired
    public R<Void> delete(@PathVariable Long id) {
        cartService.deleteItem(LoginContext.uid(), id);
        return R.ok();
    }

    @Operation(summary = "清空")
    @DeleteMapping("/clear")
    @LoginRequired
    public R<Void> clear() {
        cartService.clearCart(LoginContext.uid());
        return R.ok();
    }

    @Operation(summary = "购物车角标数量")
    @GetMapping("/count")
    @LoginRequired
    public R<Integer> count() {
        return R.ok(cartService.getCartCount(LoginContext.uid()));
    }
}
