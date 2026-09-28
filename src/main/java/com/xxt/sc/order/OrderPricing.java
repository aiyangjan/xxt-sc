package com.xxt.sc.order;

import com.xxt.sc.common.Amount;
import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;

import java.util.Arrays;

/**
 * 服务端计价。
 *
 * <p>PRD 4.1 / AC-001：客户端下单只提交 SKU 和数量，一切金额由服务端计算。
 * 优惠按<strong>订单行小计权重</strong>分摊，分摊余数归入最后一行，
 * 保证「各行实付之和 == 订单应付总额」。
 *
 * <p>金额单位统一为「分」。
 */
public final class OrderPricing {

    private OrderPricing() {
    }

    /**
     * 计算订单金额。
     *
     * @param itemSubtotalFen 每个订单行的小计（单价 × 数量），必须 ≥ 0
     * @param activityCutFen  活动优惠总额（0 表示无）
     * @param couponCutFen    优惠券优惠总额（0 表示无）
     * @return 金额快照
     * @throws BizException 参数非法或优惠超过原价
     */
    public static OrderAmount calculate(long[] itemSubtotalFen, long activityCutFen, long couponCutFen) {
        if (itemSubtotalFen == null || itemSubtotalFen.length == 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单行不能为空");
        }
        for (long v : itemSubtotalFen) {
            if (v < 0) {
                throw new BizException(ErrorCode.PARAM_ERROR, "订单行小计不能为负");
            }
        }
        if (activityCutFen < 0 || couponCutFen < 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "优惠金额不能为负");
        }

        long original = 0;
        for (long v : itemSubtotalFen) {
            original = original + v;
        }
        long totalCut = activityCutFen + couponCutFen;
        if (totalCut > original) {
            throw new BizException(ErrorCode.PARAM_ERROR, "优惠金额合计超过订单原价");
        }

        // 按行小计权重分摊；权重全为 0（整单 0 元）时退化为按行数平分
        int[] weights = toWeights(itemSubtotalFen, original);

        long[] activityParts = Amount.allocate(activityCutFen, weights);
        long[] couponParts = Amount.allocate(couponCutFen, weights);

        long[] payable = new long[itemSubtotalFen.length];
        long payableTotal = 0;
        for (int i = 0; i < itemSubtotalFen.length; i++) {
            // 单行实付不得为负
            long v = itemSubtotalFen[i] - activityParts[i] - couponParts[i];
            payable[i] = v < 0 ? 0 : v;
            payableTotal += payable[i];
        }

        long expected = original - totalCut;
        if (payableTotal != expected) {
            // 理论上不会触发；触发说明分摊出现偏差，必须暴露而不是静默
            throw new BizException(ErrorCode.INTERNAL_ERROR, "金额分摊结果与应付总额不一致");
        }

        return new OrderAmount(original, activityCutFen, couponCutFen,
                expected, activityParts, couponParts, payable);
    }

    /**
     * 行小计转 int 权重。分可能超出 int 范围，因此按比例缩放到安全区间。
     */
    private static int[] toWeights(long[] subtotals, long original) {
        int[] weights = new int[subtotals.length];
        if (original <= 0) {
            // 整单 0 元：按行数平分，避免权重和为 0
            Arrays.fill(weights, 1);
            return weights;
        }
        for (int i = 0; i < subtotals.length; i++) {
            // 缩放到 int 安全范围：占比 * 10000（保留 4 位小数精度）
            long scaled = subtotals[i] * 10000L / original;
            weights[i] = scaled <= 0 ? 1 : (int) Math.min(scaled, Integer.MAX_VALUE);
        }
        return weights;
    }
}
