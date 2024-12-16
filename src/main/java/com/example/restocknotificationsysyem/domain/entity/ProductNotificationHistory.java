package com.example.restocknotificationsysyem.domain.entity;

import com.example.restocknotificationsysyem.global.status.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductNotificationHistory extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // 외래 키
    private Product product;

    @Column(name = "restock_round", nullable = false)
    private int restockRound;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "last_sent_user_id")
    private Long lastSentUserId;

    public void updateStatus(NotificationStatus newStatus) {
        this.status = newStatus;
    }

    public void updateLastSentUserId(Long userId) {
        this.lastSentUserId = userId;
    }

}
