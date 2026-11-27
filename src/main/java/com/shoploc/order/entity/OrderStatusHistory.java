package com.shoploc.order.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "from_status")
    private String fromStatus;

    @Column(name = "to_status", nullable = false)
    private String toStatus;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(CustomerOrder order, String fromStatus, String toStatus, String actorId, String actorRole) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.changedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public CustomerOrder getOrder() { return order; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getActorId() { return actorId; }
    public String getActorRole() { return actorRole; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
