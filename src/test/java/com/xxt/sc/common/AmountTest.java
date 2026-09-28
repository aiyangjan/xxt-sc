package com.xxt.sc.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 金额工具测试。
 *
 * <p>分摊涉及真实资金，必须保证「各行之和恒等于总额」，一分都不能丢。
 */
class AmountTest {

    @Test
    void 元转分() {
        assertEquals(10000L, Amount.yuanToFen("100"));
        assertEquals(12345L, Amount.yuanToFen("123.45"));
        assertEquals(1L, Amount.yuanToFen("0.01"));
    }

    @Test
    void 分转元() {
        assertEquals("123.45", Amount.fenToYuan(12345L));
        assertEquals("0.01", Amount.fenToYuan(1L));
        assertEquals("0.00", Amount.fenToYuan(0L));
    }

    @Test
    void 分摊三份不丢分() {
        long[] r = Amount.allocate(1000L, new int[]{1, 1, 1});
        assertArrayEquals(new long[]{333, 333, 334}, r);
        assertEquals(1000L, r[0] + r[1] + r[2]);
    }

    @Test
    void 按权重分摊总额守恒() {
        long[] r = Amount.allocate(100L, new int[]{1, 2});
        assertEquals(100L, r[0] + r[1]);

        long[] r2 = Amount.allocate(9999L, new int[]{3, 5, 7, 11});
        long sum = 0;
        for (long v : r2) {
            sum += v;
        }
        assertEquals(9999L, sum);
    }

    @Test
    void 单个权重时全部归入该行() {
        long[] r = Amount.allocate(888L, new int[]{1});
        assertArrayEquals(new long[]{888L}, r);
    }

    @Test
    void 退款场景负数分摊总额守恒() {
        // 退款时金额为负，分摊仍需守恒
        long[] r = Amount.allocate(-1000L, new int[]{1, 1, 1});
        assertEquals(-1000L, r[0] + r[1] + r[2]);
    }

    @Test
    void 权重全为零时抛异常() {
        assertThrows(IllegalArgumentException.class,
                () -> Amount.allocate(100L, new int[]{0, 0}));
    }

    @Test
    void 权重为空或含负数时抛异常() {
        assertThrows(IllegalArgumentException.class,
                () -> Amount.allocate(100L, null));
        assertThrows(IllegalArgumentException.class,
                () -> Amount.allocate(100L, new int[]{}));
        assertThrows(IllegalArgumentException.class,
                () -> Amount.allocate(100L, new int[]{1, -2}));
    }

    @Test
    void 金额为空时抛异常() {
        assertThrows(IllegalArgumentException.class, () -> Amount.yuanToFen(null));
        assertThrows(IllegalArgumentException.class, () -> Amount.yuanToFen("  "));
    }
}
