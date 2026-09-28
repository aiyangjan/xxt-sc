package com.xxt.sc.order;

import com.xxt.sc.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 服务端计价测试：核心不变量是「各行实付之和 == 订单应付总额」。 */
class OrderPricingTest {

    @Test
    void 无优惠时等于原价() {
        OrderAmount a = OrderPricing.calculate(new long[]{1000L, 2000L}, 0, 0);
        assertEquals(3000L, a.getOriginalFen());
        assertEquals(3000L, a.getPayableFen());
    }

    @Test
    void 优惠按行小计权重分摊() {
        // 行1 1000 分，行2 3000 分，活动优惠 400 分 → 1:3 分摊 = 100 / 300
        OrderAmount a = OrderPricing.calculate(new long[]{1000L, 3000L}, 400L, 0);
        assertEquals(4000L, a.getOriginalFen());
        assertEquals(3600L, a.getPayableFen());
        assertArrayEquals(new long[]{100L, 300L}, a.getItemActivityCut());
        assertArrayEquals(new long[]{900L, 2700L}, a.getItemPayable());
    }

    @Test
    void 分摊后各行实付之和等于应付总额() {
        long[] rows = {1999L, 333L, 7777L};
        OrderAmount a = OrderPricing.calculate(rows, 1234L, 567L);
        long sum = 0;
        for (long v : a.getItemPayable()) {
            sum += v;
        }
        assertEquals(a.getPayableFen(), sum);
        assertEquals(rows[0] + rows[1] + rows[2] - 1234L - 567L, sum);
    }

    @Test
    void 多行三份分摊总额守恒() {
        long[] rows = {1000L, 1000L, 1000L};
        OrderAmount a = OrderPricing.calculate(rows, 1000L, 0);
        long sum = 0;
        for (long v : a.getItemPayable()) {
            sum += v;
        }
        assertEquals(2000L, sum);
    }

    @Test
    void 优惠券与活动优惠同时存在() {
        OrderAmount a = OrderPricing.calculate(new long[]{2000L, 2000L}, 500L, 500L);
        assertEquals(4000L, a.getOriginalFen());
        assertEquals(3000L, a.getPayableFen());
    }

    @Test
    void 优惠超过原价被拒绝() {
        assertThrows(BizException.class, () ->
                OrderPricing.calculate(new long[]{1000L}, 1500L, 0));
        assertThrows(BizException.class, () ->
                OrderPricing.calculate(new long[]{1000L}, 600L, 600L));
    }

    @Test
    void 非法参数被拒绝() {
        assertThrows(BizException.class, () -> OrderPricing.calculate(null, 0, 0));
        assertThrows(BizException.class, () -> OrderPricing.calculate(new long[]{}, 0, 0));
        assertThrows(BizException.class, () -> OrderPricing.calculate(new long[]{-1L}, 0, 0));
        assertThrows(BizException.class, () -> OrderPricing.calculate(new long[]{100L}, -1L, 0));
    }

    @Test
    void 整单零元时按行数平分不报错() {
        OrderAmount a = OrderPricing.calculate(new long[]{0L, 0L}, 0, 0);
        assertEquals(0L, a.getPayableFen());
    }

    @Test
    void 单行实付不为负() {
        // 100 分的行，优惠 300 分（总额未超，因另一行 1000 分）
        OrderAmount a = OrderPricing.calculate(new long[]{100L, 1000L}, 300L, 0);
        for (long v : a.getItemPayable()) {
            assertTrue(v >= 0, "单行实付不能为负");
        }
    }
}
