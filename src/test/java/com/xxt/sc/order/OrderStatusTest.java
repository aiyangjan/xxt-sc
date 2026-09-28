package com.xxt.sc.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 订单状态迁移测试。 */
class OrderStatusTest {

    @Test
    void 合法迁移() {
        assertTrue(OrderStatus.canTransit(OrderStatus.PENDING_PAY, OrderStatus.PAID));
        assertTrue(OrderStatus.canTransit(OrderStatus.PENDING_PAY, OrderStatus.CANCELLED));
        assertTrue(OrderStatus.canTransit(OrderStatus.PAID, OrderStatus.PREPARING));
        assertTrue(OrderStatus.canTransit(OrderStatus.PREPARING, OrderStatus.SHIPPED));
        assertTrue(OrderStatus.canTransit(OrderStatus.SHIPPED, OrderStatus.RECEIVED));
        assertTrue(OrderStatus.canTransit(OrderStatus.RECEIVED, OrderStatus.COMPLETED));
    }

    @Test
    void 非法迁移被拒绝() {
        // 不能从待支付直接跳到已发货
        assertFalse(OrderStatus.canTransit(OrderStatus.PENDING_PAY, OrderStatus.SHIPPED));
        // 不能从已完成回退
        assertFalse(OrderStatus.canTransit(OrderStatus.COMPLETED, OrderStatus.PAID));
        // 不能从已取消复活
        assertFalse(OrderStatus.canTransit(OrderStatus.CANCELLED, OrderStatus.PAID));
        // 已发货不能直接完成，必须先收货
        assertFalse(OrderStatus.canTransit(OrderStatus.SHIPPED, OrderStatus.COMPLETED));
    }

    @Test
    void 空值安全() {
        assertFalse(OrderStatus.canTransit(null, OrderStatus.PAID));
        assertFalse(OrderStatus.canTransit(OrderStatus.PAID, null));
    }

    @Test
    void 终态不可再迁移() {
        assertTrue(OrderStatus.COMPLETED.isFinal());
        assertTrue(OrderStatus.CANCELLED.isFinal());
        assertTrue(OrderStatus.REFUNDED.isFinal());
        assertFalse(OrderStatus.PAID.isFinal());
    }

    @Test
    void 售后相关迁移() {
        assertTrue(OrderStatus.canTransit(OrderStatus.PAID, OrderStatus.AFTER_SALE));
        assertTrue(OrderStatus.canTransit(OrderStatus.AFTER_SALE, OrderStatus.PARTIAL_REFUNDED));
        assertTrue(OrderStatus.canTransit(OrderStatus.AFTER_SALE, OrderStatus.REFUNDED));
        assertTrue(OrderStatus.canTransit(OrderStatus.PARTIAL_REFUNDED, OrderStatus.REFUNDED));
    }
}
