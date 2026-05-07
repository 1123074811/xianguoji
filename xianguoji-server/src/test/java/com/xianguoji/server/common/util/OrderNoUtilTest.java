package com.xianguoji.server.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-UT-COMMON-002: IdGenerator/OrderNoUtil测试
 * 订单号格式验证、唯一性验证
 */
class OrderNoUtilTest {

    /**
     * TC-UT-COMMON-002: 生成订单号格式正确
     */
    @Test
    void gen_shouldGenerateValidFormat_whenCalled() {
        String orderNo = OrderNoUtil.gen();

        assertNotNull(orderNo);
        assertEquals(16, orderNo.length()); // yyMMddHHmmss(12) + 6位随机数
        assertTrue(orderNo.matches("\\d{16}"));
    }

    /**
     * TC-UT-COMMON-002: 生成退款单号格式正确
     */
    @Test
    void genRefundNo_shouldGenerateValidFormat_whenCalled() {
        String refundNo = OrderNoUtil.genRefundNo();

        assertNotNull(refundNo);
        assertEquals(18, refundNo.length()); // RF + yyMMddHHmmss(12) + 6位随机数
        assertTrue(refundNo.matches("RF\\d{16}"));
    }

    /**
     * TC-UT-COMMON-002: 订单号唯一性验证（高概率）
     */
    @Test
    void gen_shouldGenerateUniqueNumbers_whenCalledMultipleTimes() {
        String orderNo1 = OrderNoUtil.gen();
        
        // 等待1毫秒确保时间戳不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
        
        String orderNo2 = OrderNoUtil.gen();

        assertNotEquals(orderNo1, orderNo2);
    }

    /**
     * TC-UT-COMMON-002: 退款单号唯一性验证
     */
    @Test
    void genRefundNo_shouldGenerateUniqueNumbers_whenCalledMultipleTimes() {
        String refundNo1 = OrderNoUtil.genRefundNo();
        
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
        
        String refundNo2 = OrderNoUtil.genRefundNo();

        assertNotEquals(refundNo1, refundNo2);
    }

    /**
     * 边界测试：时间部分格式验证
     */
    @Test
    void gen_shouldContainValidTimePart() {
        String orderNo = OrderNoUtil.gen();
        String timePart = orderNo.substring(0, 12);

        // 验证时间部分是合理的日期时间
        assertTrue(timePart.matches("\\d{12}"));
        
        // 年份应该在合理范围内（25代表2025年）
        int year = Integer.parseInt(timePart.substring(0, 2));
        assertTrue(year >= 0 && year <= 99);
        
        // 月份1-12
        int month = Integer.parseInt(timePart.substring(2, 4));
        assertTrue(month >= 1 && month <= 12);
        
        // 日期1-31
        int day = Integer.parseInt(timePart.substring(4, 6));
        assertTrue(day >= 1 && day <= 31);
        
        // 小时0-23
        int hour = Integer.parseInt(timePart.substring(6, 8));
        assertTrue(hour >= 0 && hour <= 23);
        
        // 分钟0-59
        int minute = Integer.parseInt(timePart.substring(8, 10));
        assertTrue(minute >= 0 && minute <= 59);
        
        // 秒0-59
        int second = Integer.parseInt(timePart.substring(10, 12));
        assertTrue(second >= 0 && second <= 59);
    }
}
