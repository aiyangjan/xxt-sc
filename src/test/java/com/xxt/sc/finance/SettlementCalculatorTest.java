package com.xxt.sc.finance;

import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.finance.SettlementRule.FeeBearer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 清分与冲正测试：核心不变量是「四方金额之和 == 实付 / 退款额」。 */
class SettlementCalculatorTest {

    private static final SettlementRule RULE =
            new SettlementRule("v1", 1000, 3000, FeeBearer.PLATFORM); // 平台10% 楼长30%

    @Test
    void 正常清分() {
        // 实付 10000，成本 4000，手续费 100
        SettlementResult r = SettlementCalculator.calculate(10000L, 4000L, 100L, RULE);
        assertEquals(10000L, r.totalFen(), "四方之和必须等于实付");
        assertEquals(100L, r.getFeeFen());
        // 可分配 9900 → 平台 990，楼长 2970
        assertEquals(990L, r.getPlatformFen());
        assertEquals(2970L, r.getLeaderFen());
        // 供应商 = 9900 - 990 - 2970 = 5940（高于成本 4000，不需压缩）
        assertEquals(5940L, r.getSupplierFen());
    }

    @Test
    void 任取参数守恒() {
        long[] payables = {1999L, 3333L, 88888L, 1L};
        long[] costs = {500L, 1000L, 20000L, 0L};
        long[] fees = {19L, 33L, 888L, 0L};
        for (int i = 0; i < payables.length; i++) {
            SettlementResult r =
                    SettlementCalculator.calculate(payables[i], costs[i], fees[i], RULE);
            assertEquals(payables[i], r.totalFen(), "第 " + i + " 组不守恒");
            assertTrue(r.getSupplierFen() >= 0);
            assertTrue(r.getPlatformFen() >= 0);
            assertTrue(r.getLeaderFen() >= 0);
        }
    }

    @Test
    void 分成不足以覆盖成本时优先保障供应商() {
        // 实付 1000，成本 900，手续费 0；平台10%+楼长30% → 供应商应为 600 < 900
        SettlementResult r = SettlementCalculator.calculate(1000L, 900L, 0L, RULE);
        assertEquals(900L, r.getSupplierFen(), "供应商至少拿到成本");
        assertEquals(1000L, r.totalFen(), "仍然守恒");
    }

    @Test
    void 非法参数被拒绝() {
        assertThrows(BizException.class, () ->
                SettlementCalculator.calculate(-1L, 0L, 0L, RULE));
        assertThrows(BizException.class, () ->
                SettlementCalculator.calculate(100L, 0L, 200L, RULE)); // 手续费超实付
        assertThrows(BizException.class, () ->
                SettlementCalculator.calculate(100L, 0L, 0L, null));
    }

    @Test
    void 规则构造校验() {
        assertThrows(IllegalArgumentException.class, () ->
                new SettlementRule("", 100, 100, FeeBearer.PLATFORM));
        assertThrows(IllegalArgumentException.class, () ->
                new SettlementRule("v1", 6000, 5000, FeeBearer.PLATFORM)); // 超 10000
        assertThrows(IllegalArgumentException.class, () ->
                new SettlementRule("v1", -1, 0, FeeBearer.PLATFORM));
    }

    @Test
    void 全额退款冲正() {
        SettlementResult origin = SettlementCalculator.calculate(10000L, 4000L, 100L, RULE);
        SettlementResult rev = SettlementCalculator.reverse(origin, 10000L, 10000L);
        assertEquals(10000L, rev.totalFen(), "冲正之和等于退款额");
        assertEquals(origin.getPlatformFen(), rev.getPlatformFen());
        assertEquals(origin.getLeaderFen(), rev.getLeaderFen());
    }

    @Test
    void 部分退款冲正按比例() {
        SettlementResult origin = SettlementCalculator.calculate(10000L, 4000L, 100L, RULE);
        // 退一半
        SettlementResult rev = SettlementCalculator.reverse(origin, 5000L, 10000L);
        assertEquals(5000L, rev.totalFen(), "冲正守恒");
        assertEquals(495L, rev.getPlatformFen());  // 990 的一半
        assertEquals(1485L, rev.getLeaderFen());   // 2970 的一半
    }

    @Test
    void 冲正金额超实付被拒绝() {
        SettlementResult origin = SettlementCalculator.calculate(10000L, 4000L, 100L, RULE);
        BizException e = assertThrows(BizException.class, () ->
                SettlementCalculator.reverse(origin, 10001L, 10000L));
        assertEquals("40905", e.getErrorCode().getCode());
    }

    @Test
    void 冲正非正数被拒绝() {
        SettlementResult origin = SettlementCalculator.calculate(10000L, 4000L, 100L, RULE);
        assertThrows(BizException.class, () -> SettlementCalculator.reverse(origin, 0L, 10000L));
        assertThrows(BizException.class, () -> SettlementCalculator.reverse(origin, -1L, 10000L));
    }
}
