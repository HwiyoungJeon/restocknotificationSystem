package com.example.restocknotificationsysyem.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUserNotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // 외래 키
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ProductUserNotification userNotification;

    @Column(name = "restock_round", nullable = false)
    private int restockRound;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        this.sentAt = (this.sentAt == null) ? LocalDateTime.now() : this.sentAt;
    }

    /**
     * 🔥 생성자 추가 (빌더 사용 없이 간단히 생성할 때 사용 가능)
     */
    public ProductUserNotificationHistory(Product product, ProductUserNotification userNotification, int restockRound) {
        this.product = product;
        this.userNotification = userNotification;
        this.restockRound = restockRound;
        this.sentAt = LocalDateTime.now();
    }

}