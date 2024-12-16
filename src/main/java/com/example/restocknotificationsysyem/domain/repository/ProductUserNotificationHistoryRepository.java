package com.example.restocknotificationsysyem.domain.repository;

import com.example.restocknotificationsysyem.domain.entity.ProductUserNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductUserNotificationHistoryRepository extends JpaRepository<ProductUserNotificationHistory, Long> {
    Optional<ProductUserNotificationHistory> findByProductIdAndUserNotificationIdAndRestockRound(Long productId, Long userNotificationId, int restockRound);
}