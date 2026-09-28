package com.xxt.sc.order;

import com.xxt.sc.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 退款上限测试（对应 PRD 4.3 与 AC-005）。 */
class RefundLimitTest {

    @Test
    void 可退数量计算() {
        // 已付 5 件，已退 1 件，处理中 1 件 → 可退 3 件
        assertEquals(3L, RefundLimit.refundableQuantity(5L, 1L, 1L));
    }

    @Test
    void 可退金额计算() {
        assertEquals(300L, RefundLimit.refundableAmount(1000L, 500L, 200L));
    }

    @Test
    void 超额退为0而不是负数() {
        assertEquals(0L, RefundLimit.refundableQuantity(1L, 5L, 0L));
        assertEquals(0L, RefundLimit.refundableAmount(100L, 500L, 0L));
    }

    @Test
    void 未超上限申请通过() {
        // 已付 5 件 1000 分，已退 1 件 200 分 → 可退 4 件 800 分
        assertDoesNotThrow(() -> RefundLimit.check(
                2L, 400L,
                5L, 1L, 0L,
                1000L, 200L, 0L));
    }

    @Test
    void 数量超出上限被拒绝() {
        BizException e = assertThrows(BizException.class, () -> RefundLimit.check(
                5L, 400L,
                5L, 1L, 0L,
                1000L, 200L, 0L));
        assertEquals("40905", e.getErrorCode().getCode());
    }

    @Test
    void 金额超出上限被拒绝() {
        BizException e = assertThrows(BizException.class, () -> RefundLimit.check(
                2L, 900L,
                5L, 1L, 0L,
                1000L, 200L, 0L));
        assertEquals("40905", e.getErrorCode().getCode());
    }

    @Test
    void 处理中的退款计入占用() {
        // 已付 5，已退 0，处理中 3 → 可退 2；申请 3 应被拒
        assertThrows(BizException.class, () -> RefundLimit.check(
                3L, 100L,
                5L, 0L, 3L,
                1000L, 0L, 300L));
    }

    @Test
    void 非正数申请被拒绝() {
        assertThrows(BizException.class, () -> RefundLimit.check(
                0L, 100L, 5L, 0L, 0L, 1000L, 0L, 0L));
        assertThrows(BizException.class, () -> RefundLimit.check(
                1L, 0L, 5L, 0L, 0L, 1000L, 0L, 0L));
    }

    @Test
    void 多次部分退款累计不得超上限() {
        // 已付 5 件 1000 分；第一次退 2 件 400 分成功
        assertDoesNotThrow(() -> RefundLimit.check(2L, 400L, 5L, 0L, 0L, 1000L, 0L, 0L));
        // 第二次退 3 件 600 分（剩余正好）
        assertDoesNotThrow(() -> RefundLimit.check(3L, 600L, 5L, 2L, 0L, 1000L, 400L, 0L));
        // 第三次再退 1 件 → 拒绝
        assertThrows(BizException.class, () -> RefundLimit.check(
                1L, 100L, 5L, 5L, 0L, 1000L, 1000L, 0L));
    }
}
