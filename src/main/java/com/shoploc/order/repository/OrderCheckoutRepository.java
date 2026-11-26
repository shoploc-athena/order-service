package com.shoploc.order.repository;

import com.shoploc.order.entity.OrderCheckout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderCheckoutRepository extends JpaRepository<OrderCheckout, String> {
    Optional<OrderCheckout> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
}
