package com.xxt.sc.order.api;

/** 订单行对外响应，不暴露内部成本字段。 */
public class OrderLineResponse {

    private final long skuId;
    private final int quantity;
    private final long unitPriceFen;
    private final long subtotalFen;
    private final long activityCutFen;
    private final long couponCutFen;
    private final long payableFen;

    public OrderLineResponse(long skuId, int quantity, long unitPriceFen, long subtotalFen,
                             long activityCutFen, long couponCutFen, long payableFen) {
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPriceFen = unitPriceFen;
        this.subtotalFen = subtotalFen;
        this.activityCutFen = activityCutFen;
        this.couponCutFen = couponCutFen;
        this.payableFen = payableFen;
    }

    public long getSkuId() { return skuId; }
    public int getQuantity() { return quantity; }
    public long getUnitPriceFen() { return unitPriceFen; }
    public long getSubtotalFen() { return subtotalFen; }
    public long getActivityCutFen() { return activityCutFen; }
    public long getCouponCutFen() { return couponCutFen; }
    public long getPayableFen() { return payableFen; }
}