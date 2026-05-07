package com.xianguoji.server.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * §2.5 Order 状态机单元测试
 *
 * TC-UT-ORD-STM-001: 所有合法 (from,to) 状态转换
 * TC-UT-ORD-STM-002: 非法状态转换应拒绝
 * TC-UT-ORD-STM-003: OrderStatus.of(code) 反向查找
 */
@DisplayName("OrderStatus 状态机测试")
class OrderStatusTest {

    // ==================== 合法状态转换 ====================

    @Nested
    @DisplayName("合法状态转换")
    class LegalTransitions {

        @ParameterizedTest(name = "{0} → {1} 合法")
        @CsvSource({
                "0, 1",  // PENDING_PAY → PENDING_ACCEPT (支付)
                "0, 6",  // PENDING_PAY → CANCELLED (取消)
                "1, 2",  // PENDING_ACCEPT → PREPARING (接单)
                "1, 7",  // PENDING_ACCEPT → REFUNDING (退款)
                "2, 3",  // PREPARING → DELIVERING (配送)
                "2, 4",  // PREPARING → PENDING_PICKUP (自提)
                "3, 5",  // DELIVERING → COMPLETED (确认收货)
                "4, 5",  // PENDING_PICKUP → COMPLETED (核销)
                "7, 8",  // REFUNDING → REFUNDED (退款完成)
                "7, 1",  // REFUNDING → PENDING_ACCEPT (退款驳回)
                "7, 6",  // REFUNDING → CANCELLED (全额退款→取消)
        })
        void shouldAllowLegalTransition(int fromCode, int toCode) {
            OrderStatus from = OrderStatus.of(fromCode);
            OrderStatus to = OrderStatus.of(toCode);
            assertNotNull(from);
            assertNotNull(to);
            assertTrue(isLegalTransition(from, to),
                    from.getDesc() + " → " + to.getDesc() + " 应为合法转换");
        }
    }

    // ==================== 非法状态转换 ====================

    @Nested
    @DisplayName("非法状态转换")
    class IllegalTransitions {

        @ParameterizedTest(name = "{0} → {1} 非法")
        @CsvSource({
                "5, 0",  // COMPLETED → PENDING_PAY (已完成不能回退)
                "5, 6",  // COMPLETED → CANCELLED (已完成不能取消)
                "6, 0",  // CANCELLED → PENDING_PAY (已取消不能回退)
                "6, 5",  // CANCELLED → COMPLETED (已取消不能完成)
                "8, 0",  // REFUNDED → PENDING_PAY (已退款不能回退)
                "0, 5",  // PENDING_PAY → COMPLETED (跳过中间状态)
                "0, 3",  // PENDING_PAY → DELIVERING (跳过接单)
                "1, 5",  // PENDING_ACCEPT → COMPLETED (跳过备货)
        })
        void shouldRejectIllegalTransition(int fromCode, int toCode) {
            OrderStatus from = OrderStatus.of(fromCode);
            OrderStatus to = OrderStatus.of(toCode);
            assertFalse(isLegalTransition(from, to),
                    from.getDesc() + " → " + to.getDesc() + " 应为非法转换");
        }
    }

    // ==================== OrderStatus.of() ====================

    @Nested
    @DisplayName("OrderStatus.of(code)")
    class OfCodeTests {

        @ParameterizedTest
        @EnumSource(OrderStatus.class)
        void of_shouldReturnCorrectStatus_forAllValidCodes(OrderStatus status) {
            OrderStatus result = OrderStatus.of(status.getCode());
            assertEquals(status, result);
        }

        @Test
        @DisplayName("无效 code 应抛 IllegalArgumentException")
        void of_shouldThrow_forInvalidCode() {
            assertThrows(IllegalArgumentException.class, () -> OrderStatus.of(99));
        }

        @Test
        @DisplayName("负数 code 应抛 IllegalArgumentException")
        void of_shouldThrow_forNegativeCode() {
            assertThrows(IllegalArgumentException.class, () -> OrderStatus.of(-1));
        }
    }

    // ==================== 覆盖9个状态枚举 ====================

    @Test
    @DisplayName("应有9个订单状态")
    void shouldHave9Statuses() {
        assertEquals(9, OrderStatus.values().length);
    }

    @Test
    @DisplayName("每个状态应有唯一 code")
    void shouldHaveUniqueCodes() {
        java.util.Set<Integer> codes = new java.util.HashSet<>();
        for (OrderStatus s : OrderStatus.values()) {
            assertTrue(codes.add(s.getCode()), "重复 code: " + s.getCode());
        }
    }

    // ==================== 合法转换判断 ====================

    private boolean isLegalTransition(OrderStatus from, OrderStatus to) {
        return LEGAL_TRANSITIONS.stream().anyMatch(t ->
                t.fromCode == from.getCode() && t.toCode == to.getCode());
    }

    private record Transition(int fromCode, int toCode) {}

    private static final java.util.List<Transition> LEGAL_TRANSITIONS = java.util.List.of(
            // PENDING_PAY(0) →
            new Transition(0, 1),  // 支付 → 待接单
            new Transition(0, 6),  // 取消 → 已取消
            // PENDING_ACCEPT(1) →
            new Transition(1, 2),  // 接单 → 备货中
            new Transition(1, 7),  // 退款 → 退款中
            // PREPARING(2) →
            new Transition(2, 3),  // 配送 → 配送中
            new Transition(2, 4),  // 自提 → 待自提
            // DELIVERING(3) →
            new Transition(3, 5),  // 确认收货 → 已完成
            // PENDING_PICKUP(4) →
            new Transition(4, 5),  // 核销 → 已完成
            // REFUNDING(7) →
            new Transition(7, 8),  // 退款完成 → 已退款
            new Transition(7, 1),  // 退款驳回 → 待接单
            new Transition(7, 6)   // 全额退款 → 已取消
    );
}
