package com.xianguoji.server.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * §2.1 通用/工具单元测试 - PickupCodeUtil / OrderNoUtil 补充
 */
@DisplayName("PickupCodeUtil & OrderNoUtil 补充测试")
class PickupCodeUtilTest {

    @RepeatedTest(100)
    @DisplayName("TC-UT-COMMON-002: 自提码长度应为6位纯数字")
    void gen_shouldReturn6DigitNumber() {
        String code = PickupCodeUtil.gen();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"), "自提码应为6位纯数字: " + code);
    }

    @Test
    @DisplayName("TC-UT-COMMON-002: 多次生成不应重复（概率性）")
    void gen_shouldGenerateUniqueCodes() {
        String code1 = PickupCodeUtil.gen();
        String code2 = PickupCodeUtil.gen();
        // 6位数字范围 000000~999999，连续两次相同概率极低
        assertNotEquals(code1, code2, "连续两次生成的自提码不应相同");
    }
}
