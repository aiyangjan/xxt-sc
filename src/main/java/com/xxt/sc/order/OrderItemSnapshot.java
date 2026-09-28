package com.xxt.sc.order;

/**
 * 订单行价格快照（下单时固化，后续变价不影响）。
 *
 * <p>PRD 4.1：价格变更不影响已支付订单的价格快照。
 */
public final class OrderItemSnapshot {

    private final long skuId;
    private final int quantity;
    /** 下单时单价（分） */
    private final long unitPriceFen;
    /** 小计 = 单价 × 数量 */
    private final long subtotalFen;
    /** 该行分摊的活动优惠 */
    private final long activityCutFen;
    /** 该行分摊的优惠券优惠 */
    private final long couponCutFen;
    /** 该行实付 */
    private final long payableFen;
    /** 该行成本（分），用于利润与供应商结算；无权限时不得返回前端 */
    private final long costFen;

    public OrderItemSnapshot(long skuId, int quantity, long unitPriceFen, long subtotalFen,
                             long activityCutFen, long couponCutFen, long payableFen, long costFen) {
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPriceFen = unitPriceFen;
        this.subtotalFen = subtotalFen;
        this.activityCutFen = activityCutFen;
        this.couponCutFen = couponCutFen;
        this.payableFen = payableFen;
        this.costFen = costFen;
    }

    public long getSkuId() {
        return skuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getUnitPriceFen() {
        return unitPriceFen;
    }

    public long getSubtotalFen() {
        return subtotalFen;
    }

    public long getActivityCutFen() {
        return activityCutFen;
    }

    public long getCouponCutFen() {
        return couponCutFen;
    }

    public long getPayableFen() {
        return payableFen;
    }

    public long getCostFen() {
        return costFen;
    }
}
