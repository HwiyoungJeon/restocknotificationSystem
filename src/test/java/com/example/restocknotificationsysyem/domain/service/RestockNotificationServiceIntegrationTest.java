package com.example.restocknotificationsysyem.domain.service;

import com.example.restocknotificationsysyem.domain.dto.RestockResponseDto;
import com.example.restocknotificationsysyem.domain.entity.*;
import com.example.restocknotificationsysyem.domain.repository.*;
import com.example.restocknotificationsysyem.global.status.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class RestockNotificationServiceIntegrationTest {

    @Autowired
    private RestockNotificationService restockNotificationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductNotificationHistoryRepository productNotificationHistoryRepository;

    @Autowired
    private ProductUserNotificationRepository productUserNotificationRepository;

    @Autowired
    private ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;

    private Product product;

//    @BeforeEach
//    void setUp() {
//        // 🔥 데이터 초기화
//        productUserNotificationHistoryRepository.deleteAll();
//        productNotificationHistoryRepository.deleteAll();
//        productUserNotificationRepository.deleteAll();
//        productRepository.deleteAll();
//
//        // 🔥 새로운 데이터 삽입
//        product = Product.builder()
//                .stock(10)
//                .previousStock(5)
//                .restockRound(1)
//                .build();
//        productRepository.save(product);
//
//        ProductUserNotification user1 = ProductUserNotification.builder()
//                .product(product)
//                .userId(101L)
//                .isActive(true)
//                .build();
//
//        ProductUserNotification user2 = ProductUserNotification.builder()
//                .product(product)
//                .userId(102L)
//                .isActive(true)
//                .build();
//
//        productUserNotificationRepository.save(user1);
//        productUserNotificationRepository.save(user2);
//    }


    @BeforeEach
    void setUp() {
        // 🔥 데이터 초기화
        productUserNotificationHistoryRepository.deleteAll();
        productNotificationHistoryRepository.deleteAll();
        productUserNotificationRepository.deleteAll();
        productRepository.deleteAll();

        // 🔥 새로운 데이터 삽입
        product = Product.builder()
                .stock(1) // 재고 1로 설정 (재고 소진 테스트를 위해)
                .previousStock(5)
                .restockRound(1)
                .build();
        productRepository.save(product);

        ProductUserNotification user1 = ProductUserNotification.builder()
                .product(product)
                .userId(101L)
                .isActive(true)
                .build();

        ProductUserNotification user2 = ProductUserNotification.builder()
                .product(product)
                .userId(102L)
                .isActive(true)
                .build();

        productUserNotificationRepository.save(user1);
        productUserNotificationRepository.save(user2);
    }

    @Test
    @DisplayName("통합 테스트: 상품 재입고가 정상적으로 이루어진다.")
    void restockProduct_Success() {
        // When
        RestockResponseDto responseDto = restockNotificationService.restockProduct(product.getId());

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getRestockRound()).isEqualTo(2); // 🔥 재입고 회차가 1 -> 2 증가

        // 알림 내역 확인
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository
                .findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow();

        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.COMPLETED); // 🔥 알림 완료 상태 확인

        // 알림이 두 명에게 전송되었는지 확인
        List<ProductUserNotificationHistory> notificationHistories = productUserNotificationHistoryRepository.findAll();
        assertThat(notificationHistories).hasSize(2);
    }

    @Test
    @DisplayName("통합 테스트: 재입고 도중 품절이 발생하면 알림이 중단된다.")
    void restockProduct_StopsWhenOutOfStock() {
        // 🔥 상품의 재고를 1로 설정 (두 명에게 알림을 보내다가 중단됨)
        product.addStock(1); // 재고 1로 설정
        productRepository.save(product);

        // When
        RestockResponseDto responseDto = restockNotificationService.restockProduct(product.getId());

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(0); // 🔥 재고가 0이어야 함 (품절)

        // 알림 내역 확인
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository
                .findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow();

        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.CANCELED_BY_SOLD_OUT); // 🔥 품절로 인한 중단 상태

        // 알림이 한 명에게만 전송되었는지 확인
        List<ProductUserNotificationHistory> notificationHistories = productUserNotificationHistoryRepository.findAll();
        assertThat(notificationHistories).hasSize(1); // 🔥 알림 1개만 있어야 함
    }

    @Test
    @DisplayName("통합 테스트: 알림 재발송이 정상적으로 이루어진다.")
    void retryNotification_Success() {
        // 🔥 1차 알림 전송 (2명의 유저에게 전송됨)
        restockNotificationService.restockProduct(product.getId());

        // 🔥 2차 알림 재발송 (첫 번째 유저는 이미 받았으므로 두 번째 유저부터 다시 시작)
        RestockResponseDto responseDto = restockNotificationService.retryNotification(product.getId());

        // Then
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository
                .findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow();

        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.COMPLETED); // 🔥 재발송이 완료되었는지 확인

        // 알림 이력 확인 (이미 발송된 유저를 제외하고 재발송이 이루어졌는지 확인)
        List<ProductUserNotificationHistory> notificationHistories = productUserNotificationHistoryRepository.findAll();
        assertThat(notificationHistories).hasSize(4); // 🔥 총 2명 * 2번 발송 = 4개의 알림 기록
    }

    @Test
    @DisplayName("통합 테스트: 상품 조회 후 예외가 발생한다.")
    void findProductById_Fail_ProductNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> restockNotificationService.findProductById(999L));
    }

    @Test
    @DisplayName("통합 테스트: 재입고 이력이 없을 때 예외가 발생한다.")
    void retryNotification_Fail_NoNotificationHistory() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> restockNotificationService.retryNotification(999L));
    }
}
