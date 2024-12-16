package com.example.restocknotificationsysyem.domain.repository;


import com.example.restocknotificationsysyem.domain.entity.ProductUserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductUserNotificationRepository extends JpaRepository<ProductUserNotification, Long> {
    List<ProductUserNotification> findByProductIdAndIsActiveTrue(Long productId);
}
