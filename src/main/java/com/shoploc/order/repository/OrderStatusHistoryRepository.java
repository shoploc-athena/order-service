package com.shoploc.order.repository;

import com.shoploc.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(String orderId);
}
