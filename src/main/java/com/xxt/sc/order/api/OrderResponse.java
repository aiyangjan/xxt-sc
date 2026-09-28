package com.xxt.sc.order.api;

import com.xxt.sc.order.OrderStatus;

import java.util.List;

/** 订单对外响应。 */
public class OrderResponse {

    private final String orderNo;
    private final OrderStatus status;
    private final long originalFen;
    private final long activityCutFen;
    private final long couponCutFen;
    private final long payableFen;
    private final List<OrderLineResponse> items;

    public OrderResponse(String orderNo, OrderStatus status, long originalFen, long activityCutFen,
                         long couponCutFen, long payableFen, List<OrderLineResponse> items) {
        this.orderNo = orderNo;
        this.status = status;
        this.originalFen = originalFen;
        this.activityCutFen = activityCutFen;
        this.couponCutFen = couponCutFen;
        this.payableFen = payableFen;
        this.items = List.copyOf(items);
    }

    public String getOrderNo() { return orderNo; }
    public OrderStatus getStatus() { return status; }
    public long getOriginalFen() { return originalFen; }
    public long getActivityCutFen() { return activityCutFen; }
    public long getCouponCutFen() { return couponCutFen; }
    public long getPayableFen() { return payableFen; }
    public List<OrderLineResponse> getItems() { return items; }
}