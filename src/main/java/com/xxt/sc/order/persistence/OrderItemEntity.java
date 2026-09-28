package com.xxt.sc.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "xxt_order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price_fen", nullable = false)
    private long unitPriceFen;

    @Column(name = "subtotal_fen", nullable = false)
    private long subtotalFen;

    @Column(name = "activity_cut_fen", nullable = false)
    private long activityCutFen;

    @Column(name = "coupon_cut_fen", nullable = false)
    private long couponCutFen;

    @Column(name = "payable_fen", nullable = false)
    private long payableFen;

    protected OrderItemEntity() {
    }

    public OrderItemEntity(Long skuId, int quantity, long unitPriceFen, long subtotalFen,
                           long activityCutFen, long couponCutFen, long payableFen) {
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPriceFen = unitPriceFen;
        this.subtotalFen = subtotalFen;
        this.activityCutFen = activityCutFen;
        this.couponCutFen = couponCutFen;
        this.payableFen = payableFen;
    }

    void attachTo(OrderEntity order) { this.order = order; }

    public Long getId() { return id; }
    public Long getSkuId() { return skuId; }
    public int getQuantity() { return quantity; }
    public long getUnitPriceFen() { return unitPriceFen; }
    public long getSubtotalFen() { return subtotalFen; }
    public long getActivityCutFen() { return activityCutFen; }
    public long getCouponCutFen() { return couponCutFen; }
    public long getPayableFen() { return payableFen; }
}
