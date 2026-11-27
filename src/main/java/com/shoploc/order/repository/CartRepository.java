package com.shoploc.order.repository;

import com.shoploc.order.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByCustomerId(String customerId);
}
