package com.shoploc.order.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "order_checkout", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "idempotency_key"}))
public class OrderCheckout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestHash;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected OrderCheckout() {
    }

    public OrderCheckout(String customerId, String requestHash, Long totalAmount, String idempotencyKey) {
        this.customerId = customerId;
        this.requestHash = requestHash;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public Long getTotalAmount() { return totalAmount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
