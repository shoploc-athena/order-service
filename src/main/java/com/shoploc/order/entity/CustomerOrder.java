package com.shoploc.order.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id")
    private OrderCheckout checkout;

    @Column(nullable = false)
    private String customerId;

    @Column(name = "customer_display_name")
    private String customerDisplayName;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private String status;

    @Column(name = "pickup_code")
    private String pickupCode;

    @Column(nullable = false)
    private OffsetDateTime pickupSlotStart;

    @Column(nullable = false)
    private OffsetDateTime pickupSlotEnd;

    @Column(name = "pickup_delay_min")
    private Integer pickupDelayMin;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private String currency = "EUR";

    @Column(nullable = false)
    private String paymentMode;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "collected_at")
    private OffsetDateTime collectedAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String customerId, String storeId, String storeName,
                         OffsetDateTime pickupSlotStart, OffsetDateTime pickupSlotEnd,
                         Long totalAmount, String paymentMode, String paymentStatus) {
        this.customerId = customerId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.pickupSlotStart = pickupSlotStart;
        this.pickupSlotEnd = pickupSlotEnd;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
        this.status = "CONFIRMED";
        this.currency = "EUR";
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        this.version = 0L;
    }

    public String getId() { return id; }
    public OrderCheckout getCheckout() { return checkout; }
    public String getCustomerId() { return customerId; }
    public String getCustomerDisplayName() { return customerDisplayName; }
    public String getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public String getStatus() { return status; }
    public String getPickupCode() { return pickupCode; }
    public OffsetDateTime getPickupSlotStart() { return pickupSlotStart; }
    public OffsetDateTime getPickupSlotEnd() { return pickupSlotEnd; }
    public Integer getPickupDelayMin() { return pickupDelayMin; }
    public Long getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getPaymentMode() { return paymentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentReference() { return paymentReference; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public OffsetDateTime getCollectedAt() { return collectedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public List<OrderLine> getLines() { return lines; }
    public List<OrderStatusHistory> getStatusHistory() { return statusHistory; }

    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setCollectedAt(OffsetDateTime collectedAt) { this.collectedAt = collectedAt; }
    public void setCustomerDisplayName(String customerDisplayName) { this.customerDisplayName = customerDisplayName; }
    public void setPickupDelayMin(Integer pickupDelayMin) { this.pickupDelayMin = pickupDelayMin; }
}
