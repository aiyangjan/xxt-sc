package com.xxt.sc.order.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNo(String orderNo);
}
