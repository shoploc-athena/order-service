package com.shoploc.order.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(nullable = false)
    private String articleId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Long unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long lineTotal;

    protected OrderLine() {
    }

    public OrderLine(CustomerOrder order, String articleId, String label, Long unitPrice, Integer quantity) {
        this.order = order;
        this.articleId = articleId;
        this.label = label;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = unitPrice * quantity;
    }

    public String getId() { return id; }
    public CustomerOrder getOrder() { return order; }
    public String getArticleId() { return articleId; }
    public String getLabel() { return label; }
    public Long getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public Long getLineTotal() { return lineTotal; }
}
