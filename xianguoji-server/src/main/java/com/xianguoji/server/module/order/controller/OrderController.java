package com.xianguoji.server.module.order.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.Idempotent;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.order.dto.*;
import com.xianguoji.server.module.order.service.OrderService;
import com.xianguoji.server.module.order.vo.OrderPreviewVO;
import com.xianguoji.server.module.order.vo.OrderVO;
import com.xianguoji.server.module.order.vo.RefundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单-用户端")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "结算预览")
    @PostMapping("/api/u/order/preview")
    @LoginRequired
    public R<OrderPreviewVO> preview(@Valid @RequestBody OrderPreviewDto dto) {
        return R.ok(orderService.preview(LoginContext.uid(), dto));
    }

    @Operation(summary = "提交订单")
    @PostMapping("/api/u/order/submit")
    @LoginRequired
    public R<Map<String, String>> submit(@Valid @RequestBody OrderSubmitDto dto) {
        String orderNo = orderService.submit(LoginContext.uid(), dto);
        return R.ok(Map.of("orderNo", orderNo));
    }

    @Operation(summary = "发起支付")
    @PostMapping("/api/u/order/{orderNo}/pay")
    @LoginRequired
    @Idempotent(key = "order:pay", ttl = 5, requestParam = "orderNo")
    public R<Object> pay(@PathVariable String orderNo) {
        return R.ok(orderService.pay(LoginContext.uid(), orderNo));
    }

    @Operation(summary = "订单列表")
    @GetMapping("/api/u/order/page")
    @LoginRequired
    public R<PageVO<OrderVO>> page(OrderQry qry) {
        return R.ok(orderService.userOrderPage(LoginContext.uid(), qry));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/api/u/order/{orderNo}")
    @LoginRequired
    public R<OrderVO> detail(@PathVariable String orderNo) {
        return R.ok(orderService.getOrderDetail(LoginContext.uid(), orderNo));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/api/u/order/{orderNo}/cancel")
    @LoginRequired
    public R<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelOrder(LoginContext.uid(), orderNo);
        return R.ok();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/api/u/order/{orderNo}/confirm")
    @LoginRequired
    public R<Void> confirm(@PathVariable String orderNo) {
        orderService.confirmReceive(LoginContext.uid(), orderNo);
        return R.ok();
    }

    @Operation(summary = "提醒发货")
    @PostMapping("/api/u/order/{orderNo}/remind")
    @LoginRequired
    public R<Void> remind(@PathVariable String orderNo) {
        orderService.remindShip(LoginContext.uid(), orderNo);
        return R.ok();
    }

    @Operation(summary = "再来一单")
    @PostMapping("/api/u/order/{orderNo}/repurchase")
    @LoginRequired
    public R<Void> repurchase(@PathVariable String orderNo) {
        orderService.repurchase(LoginContext.uid(), orderNo);
        return R.ok();
    }

    @Operation(summary = "申请售后")
    @PostMapping("/api/u/refund")
    @LoginRequired
    public R<Void> applyRefund(@Valid @RequestBody RefundApplyDto dto) {
        orderService.applyRefund(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "售后详情")
    @GetMapping("/api/u/refund/{refundNo}")
    @LoginRequired
    public R<RefundVO> refundDetail(@PathVariable String refundNo) {
        return R.ok(orderService.getRefundDetail(LoginContext.uid(), refundNo));
    }
}
