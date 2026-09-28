package com.xxt.sc.order.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OrderJpaRepositoryTest {

    @Autowired
    private OrderJpaRepository repository;

    @Test
    void storesOrderAndImmutableLineSnapshot() {
        Instant now = Instant.parse("2026-09-28T00:00:00Z");
        OrderEntity order = new OrderEntity("XO-P1-0001", "PENDING_PAY", 398, 0, 0, 398, now);
        order.addItem(new OrderItemEntity(10001L, 2, 199, 398, 0, 0, 398));

        OrderEntity saved = repository.saveAndFlush(order);

        OrderEntity loaded = repository.findByOrderNo("XO-P1-0001").orElseThrow();
        assertThat(saved.getId()).isNotNull();
        assertThat(loaded.getStatus()).isEqualTo("PENDING_PAY");
        assertThat(loaded.getPayableFen()).isEqualTo(398);
        assertThat(loaded.getItems()).singleElement()
                .extracting(OrderItemEntity::getUnitPriceFen, OrderItemEntity::getQuantity)
                .containsExactly(199L, 2);
    }
}
