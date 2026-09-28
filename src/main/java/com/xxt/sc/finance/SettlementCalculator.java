package com.xxt.sc.finance;

import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;

/**
 * 清分与分润计算。
 *
 * <p>PRD 4.5：楼长利润、平台利润、供应商应结分别计算，明确优惠承担方、
 * 退款责任、手续费承担方与分润规则版本。
 *
 * <p>核心不变量：<strong>平台 + 楼长 + 供应商 + 手续费 == 订单行实付</strong>。
 * 一旦不等就是资金对不上，直接暴露而不是静默。
 *
 * <p>PRD 4.3：清分后退款不删除原流水，而是生成<strong>冲正记录</strong>，
 * 见 {@link #reverse(SettlementResult, long, long, long, long)}。
 */
public final class SettlementCalculator {

    private SettlementCalculator() {
    }

    /**
     * 计算一笔订单行的清分结果。
     *
     * @param payableFen 该行实付（分）
     * @param costFen    该行成本（分），用于供应商应结
     * @param feeFen     交易手续费（分）
     * @param rule       分润规则
     * @return 清分结果
     * @throws BizException 参数非法或结果不守恒
     */
    public static SettlementResult calculate(long payableFen, long costFen, long feeFen, SettlementRule rule) {
        if (rule == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "分润规则不能为空");
        }
        if (payableFen < 0 || costFen < 0 || feeFen < 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "金额不能为负");
        }
        if (feeFen > payableFen) {
            throw new BizException(ErrorCode.PARAM_ERROR, "手续费不能超过实付金额");
        }

        // 可分配金额 = 实付 - 手续费
        long distributable = payableFen - feeFen;

        long platform = distributable * rule.getPlatformRateBp() / 10_000L;
        long leader = distributable * rule.getLeaderRateBp() / 10_000L;

        // 供应商应结 = 成本优先，剩余为平台留存？此处按「可分配扣除平台与楼长后的余额」，
        // 但不得低于其成本（成本是供应商的底线）
        long supplier = distributable - platform - leader;
        if (supplier < costFen) {
            // 分成后不足以覆盖成本：压缩平台与楼长分成，优先保障成本
            // （业务上也可选择记负差额待人工处理，此处选择保护供应商并暴露差异）
            long gap = costFen - supplier;
            long takeFromLeader = Math.min(leader, gap);
            leader -= takeFromLeader;
            gap -= takeFromLeader;
            long takeFromPlatform = Math.min(platform, gap);
            platform -= takeFromPlatform;
            supplier = costFen;
        }

        long total = platform + leader + supplier + feeFen;
        if (total != payableFen) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "清分结果与实付不一致，禁止入账");
        }

        return new SettlementResult(platform, leader, supplier, feeFen, rule.getVersion());
    }

    /**
     * 清分后退款的冲正计算。
     *
     * <p>PRD 4.3：清分后的退款生成平台、楼长、供应商、手续费的<strong>冲正记录</strong>，
     * 不删除原流水。冲正金额按原清分比例反向计算，余数归供应商以保证守恒。
     *
     * @param original 原清分结果
     * @param refundFen     本次退款金额（分）
     * @param paidFen       该行原实付（分）
     * @return 冲正结果（各项为需要扣回的金额，正值表示扣减）
     */
    public static SettlementResult reverse(SettlementResult original, long refundFen, long paidFen) {
        if (original == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "原清分结果不能为空");
        }
        if (refundFen <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "冲正金额必须大于 0");
        }
        if (refundFen > paidFen) {
            throw new BizException(ErrorCode.REFUND_EXCEED_LIMIT, "冲正金额超过原实付");
        }
        if (paidFen <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "原实付金额必须大于 0");
        }

        long platform = original.getPlatformFen() * refundFen / paidFen;
        long leader = original.getLeaderFen() * refundFen / paidFen;
        long fee = original.getFeeFen() * refundFen / paidFen;
        // 余数归供应商，保证四项之和 == 退款金额
        long supplier = refundFen - platform - leader - fee;

        return new SettlementResult(platform, leader, supplier, fee, original.getRuleVersion());
    }
}
