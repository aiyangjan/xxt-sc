package com.xxt.sc.order.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * P1 订单持久化模型。金额统一使用分，订单行保留下单时快照。
 * 该模型与 API DTO 分离，避免商品价格变化影响历史订单。
 */
@Entity
@Table(name = "xxt_orders", uniqueConstraints = @UniqueConstraint(name = "uk_xxt_orders_order_no", columnNames = "order_no"))
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 32)
    private String orderNo;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "original_amount_fen", nullable = false)
    private long originalAmountFen;

    @Column(name = "activity_cut_fen", nullable = false)
    private long activityCutFen;

    @Column(name = "coupon_cut_fen", nullable = false)
    private long couponCutFen;

    @Column(name = "payable_fen", nullable = false)
    private long payableFen;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(String orderNo, String status, long originalAmountFen, long activityCutFen,
                       long couponCutFen, long payableFen, Instant now) {
        this.orderNo = orderNo;
        this.status = status;
        this.originalAmountFen = originalAmountFen;
        this.activityCutFen = activityCutFen;
        this.couponCutFen = couponCutFen;
        this.payableFen = payableFen;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void addItem(OrderItemEntity item) {
        item.attachTo(this);
        items.add(item);
    }

    public void changeStatus(String status, Instant now) {
        this.status = status;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public String getStatus() { return status; }
    public long getOriginalAmountFen() { return originalAmountFen; }
    public long getActivityCutFen() { return activityCutFen; }
    public long getCouponCutFen() { return couponCutFen; }
    public long getPayableFen() { return payableFen; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItemEntity> getItems() { return List.copyOf(items); }
}
