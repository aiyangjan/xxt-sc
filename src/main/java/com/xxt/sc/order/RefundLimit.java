package com.xxt.sc.order;

import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;

/**
 * 退款上限计算（订单行粒度）。
 *
 * <p>PRD 4.3 / AC-005：
 * <ul>
 *   <li>可退数量 = 已支付数量 − 已退款数量 − 退款处理中数量</li>
 *   <li>可退金额 = 该行已支付金额 − 已退款金额 − 处理中金额</li>
 *   <li>一笔订单可多次部分退款，但数量与金额总和不得超过上限</li>
 * </ul>
 *
 * <p>所有校验必须在服务端完成，前端计算只用于展示。
 */
public final class RefundLimit {

    private RefundLimit() {
    }

    /**
     * 可退数量。
     */
    public static long refundableQuantity(long paidQty, long refundedQty, long processingQty) {
        long v = paidQty - refundedQty - processingQty;
        return v < 0 ? 0 : v;
    }

    /**
     * 可退金额（分）。
     */
    public static long refundableAmount(long paidFen, long refundedFen, long processingFen) {
        long v = paidFen - refundedFen - processingFen;
        return v < 0 ? 0 : v;
    }

    /**
     * 校验一次退款申请是否越界。
     *
     * @param requestQty     本次申请退的数量
     * @param requestFen     本次申请退的金额（分）
     * @param paidQty        该行已支付数量
     * @param refundedQty    该行已退款数量
     * @param processingQty  该行退款处理中数量
     * @param paidFen        该行已支付金额
     * @param refundedFen    该行已退款金额
     * @param processingFen  该行处理中金额
     * @throws BizException REFUND_EXCEED_LIMIT 超出可退上限；PARAM_ERROR 申请值非正
     */
    public static void check(long requestQty, long requestFen,
                             long paidQty, long refundedQty, long processingQty,
                             long paidFen, long refundedFen, long processingFen) {
        if (requestQty <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "退款数量必须大于 0");
        }
        if (requestFen <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "退款金额必须大于 0");
        }

        long maxQty = refundableQuantity(paidQty, refundedQty, processingQty);
        if (requestQty > maxQty) {
            throw new BizException(ErrorCode.REFUND_EXCEED_LIMIT,
                    "退款数量超出可退上限，可退 " + maxQty + " 件");
        }

        long maxFen = refundableAmount(paidFen, refundedFen, processingFen);
        if (requestFen > maxFen) {
            throw new BizException(ErrorCode.REFUND_EXCEED_LIMIT,
                    "退款金额超出可退上限，可退 " + maxFen + " 分");
        }
    }
}
