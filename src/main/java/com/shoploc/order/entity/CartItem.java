package com.shoploc.order.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cart_item", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "article_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String articleId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime addedAt;

    protected CartItem() {
    }

    public CartItem(Cart cart, String storeId, String articleId, Integer quantity) {
        this.cart = cart;
        this.storeId = storeId;
        this.articleId = articleId;
        this.quantity = quantity;
        this.addedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public Cart getCart() { return cart; }
    public String getStoreId() { return storeId; }
    public String getArticleId() { return articleId; }
    public Integer getQuantity() { return quantity; }
    public OffsetDateTime getAddedAt() { return addedAt; }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
