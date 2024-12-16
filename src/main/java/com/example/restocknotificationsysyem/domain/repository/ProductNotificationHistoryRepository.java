package com.example.restocknotificationsysyem.domain.repository;

import com.example.restocknotificationsysyem.domain.entity.ProductNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductNotificationHistoryRepository extends JpaRepository<ProductNotificationHistory, Long> {
    Optional<ProductNotificationHistory> findTopByProductIdOrderByCreatedAtDesc(Long productId);

}