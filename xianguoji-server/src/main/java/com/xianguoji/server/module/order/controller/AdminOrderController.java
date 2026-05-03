package com.xianguoji.server.module.order.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.order.dto.AdminOrderQry;
import com.xianguoji.server.module.order.service.OrderService;
import com.xianguoji.server.module.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单-商家端")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "多条件分页")
    @GetMapping("/order/page")
    @AdminRequired
    public R<PageVO<OrderVO>> page(AdminOrderQry qry) {
        return R.ok(orderService.adminOrderPage(qry));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/{orderNo}")
    @AdminRequired
    public R<OrderVO> detail(@PathVariable String orderNo) {
        return R.ok(orderService.adminOrderDetail(orderNo));
    }

    @Operation(summary = "接单")
    @PostMapping("/order/{orderNo}/accept")
    @AdminRequired
    public R<Void> accept(@PathVariable String orderNo) {
        orderService.acceptOrder(orderNo);
        return R.ok();
    }

    @Operation(summary = "拒单")
    @PostMapping("/order/{orderNo}/reject")
    @AdminRequired
    public R<Void> reject(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        orderService.rejectOrder(orderNo, body.get("reason"));
        return R.ok();
    }

    @Operation(summary = "标记出库")
    @PostMapping("/order/{orderNo}/ship")
    @AdminRequired
    public R<Void> ship(@PathVariable String orderNo, @RequestBody Map<String, Object> body) {
        Integer deliveryType = body.get("deliveryType") != null ? (Integer) body.get("deliveryType") : null;
        String courierName = (String) body.get("courierName");
        String courierPhone = (String) body.get("courierPhone");
        orderService.shipOrder(orderNo, deliveryType, courierName, courierPhone);
        return R.ok();
    }

    @Operation(summary = "核销自提码")
    @PostMapping("/order/{orderNo}/pickup-verify")
    @AdminRequired
    public R<Void> pickupVerify(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        orderService.pickupVerify(orderNo, body.get("pickupCode"));
        return R.ok();
    }

    @Operation(summary = "打印小票")
    @PostMapping("/order/{orderNo}/print")
    @AdminRequired
    public R<Object> print(@PathVariable String orderNo) {
        return R.ok(orderService.printReceipt(orderNo));
    }

    @Operation(summary = "新订单数量")
    @GetMapping("/order/new-count")
    @AdminRequired
    public R<Integer> newCount(@RequestParam long since) {
        return R.ok(orderService.newOrderCount(since));
    }

    @Operation(summary = "同意退款")
    @PostMapping("/refund/{refundNo}/approve")
    @AdminRequired
    public R<Void> approveRefund(@PathVariable String refundNo) {
        orderService.approveRefund(refundNo);
        return R.ok();
    }

    @Operation(summary = "拒绝退款")
    @PostMapping("/refund/{refundNo}/reject")
    @AdminRequired
    public R<Void> rejectRefund(@PathVariable String refundNo, @RequestBody Map<String, String> body) {
        orderService.rejectRefund(refundNo, body.get("reason"));
        return R.ok();
    }
}
