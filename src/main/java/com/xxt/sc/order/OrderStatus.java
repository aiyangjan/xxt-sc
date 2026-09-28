package com.xxt.sc.order;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 订单主状态。
 *
 * <p>PRD 4.2：订单、支付、履约、售后、清分、ERP 同步各有独立状态，
 * 本枚举只描述<strong>订单主状态</strong>，其余状态另建字段，禁止合并。
 *
 * <p>迁移表需在业务确认后固化（对应待补充项 T-01）。当前为可执行的合理版本。
 */
public enum OrderStatus {

    /** 待支付 */
    PENDING_PAY("待支付"),
    /** 已支付 */
    PAID("已支付"),
    /** 备货中（楼长已接单） */
    PREPARING("备货中"),
    /** 已发货 */
    SHIPPED("已发货"),
    /** 已收货 */
    RECEIVED("已收货"),
    /** 已完成（清分入账完成） */
    COMPLETED("已完成"),
    /** 已取消（未支付关闭，或退款后关闭） */
    CANCELLED("已取消"),
    /** 售后中（存在处理中的退款） */
    AFTER_SALE("售后中"),
    /** 部分退款 */
    PARTIAL_REFUNDED("部分退款"),
    /** 已退款（全额） */
    REFUNDED("已退款");

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = buildTransitions();

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> m = new EnumMap<>(OrderStatus.class);
        m.put(PENDING_PAY, EnumSet.of(PAID, CANCELLED));
        m.put(PAID, EnumSet.of(PREPARING, AFTER_SALE, CANCELLED));
        m.put(PREPARING, EnumSet.of(SHIPPED, AFTER_SALE, CANCELLED));
        m.put(SHIPPED, EnumSet.of(RECEIVED, AFTER_SALE));
        m.put(RECEIVED, EnumSet.of(COMPLETED, AFTER_SALE));
        m.put(AFTER_SALE, EnumSet.of(PARTIAL_REFUNDED, REFUNDED, COMPLETED));
        m.put(PARTIAL_REFUNDED, EnumSet.of(REFUNDED, COMPLETED, AFTER_SALE));
        // 终态
        m.put(COMPLETED, EnumSet.noneOf(OrderStatus.class));
        m.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        m.put(REFUNDED, EnumSet.noneOf(OrderStatus.class));
        return m;
    }

    /**
     * 是否允许从 from 迁移到 to。
     *
     * @return true 表示合法迁移
     */
    public static boolean canTransit(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        Set<OrderStatus> next = TRANSITIONS.get(from);
        return next != null && next.contains(to);
    }

    /** 该状态允许的后续状态（只读）。 */
    public static Set<OrderStatus> nextOf(OrderStatus from) {
        if (from == null) {
            return Collections.emptySet();
        }
        Set<OrderStatus> next = TRANSITIONS.get(from);
        return next == null ? Collections.emptySet() : Collections.unmodifiableSet(next);
    }

    /** 是否为终态（不可再迁移）。 */
    public boolean isFinal() {
        Set<OrderStatus> next = TRANSITIONS.get(this);
        return next == null || next.isEmpty();
    }
}
