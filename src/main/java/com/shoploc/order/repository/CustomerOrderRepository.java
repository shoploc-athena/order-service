package com.shoploc.order.repository;

import com.shoploc.order.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, String> {
    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<CustomerOrder> findByStoreIdAndStatusIn(String storeId, List<String> statuses);
    Optional<CustomerOrder> findByStoreIdAndPickupCode(String storeId, String pickupCode);
}
