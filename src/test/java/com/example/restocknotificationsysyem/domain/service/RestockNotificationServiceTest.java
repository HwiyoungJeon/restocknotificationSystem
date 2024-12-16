package com.example.restocknotificationsysyem.domain.service;

import com.example.restocknotificationsysyem.domain.dto.RestockResponseDto;
import com.example.restocknotificationsysyem.domain.entity.*;
import com.example.restocknotificationsysyem.domain.repository.*;
import com.example.restocknotificationsysyem.global.exception.RestockException;
import com.example.restocknotificationsysyem.global.status.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RestockNotificationServiceTest {

    @InjectMocks
    private RestockNotificationService restockNotificationService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductNotificationHistoryRepository productNotificationHistoryRepository;

    @Mock
    private ProductUserNotificationRepository productUserNotificationRepository;

    @Mock
    private ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("성공: 상품을 찾는다.")
    void findProductById_Success() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().id(productId).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        Product result = restockNotificationService.findProductById(productId);

        // Then
        assertThat(result).isEqualTo(product);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 상품을 조회할 때 예외 발생")
    void findProductById_Fail_ProductNotFound() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestockException.class,
                () -> restockNotificationService.findProductById(1L));
    }

    @Test
    @DisplayName("성공: 알림 히스토리를 생성한다.")
    void createNotificationHistory_Success() {
        // Given
        Product product = Product.builder().id(1L).build();
        ProductNotificationHistory history = ProductNotificationHistory.builder()
                .product(product)
                .status(NotificationStatus.IN_PROGRESS)
                .build();
        when(productNotificationHistoryRepository.save(any(ProductNotificationHistory.class)))
                .thenReturn(history);

        // When
        ProductNotificationHistory result = restockNotificationService.createNotificationHistory(product);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.IN_PROGRESS);
        verify(productNotificationHistoryRepository, times(1)).save(any(ProductNotificationHistory.class));
    }

    @Test
    @DisplayName("성공: 상품의 재입고 회차가 1 증가한다.")
    void increaseRestockRound_Success() {
        // Given
        Product product = Product.builder().id(1L).restockRound(2).build();

        // When
        product.increaseRestockRound();

        // Then
        assertThat(product.getRestockRound()).isEqualTo(3);
    }

    @Test
    @DisplayName("성공: 알림 전송 후 상태가 COMPLETED로 업데이트 된다.")
    void updateNotificationStatus_Completed() {
        // Given
        ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder()
                .status(NotificationStatus.IN_PROGRESS)
                .build();

        // When
        restockNotificationService.updateNotificationStatus(notificationHistory, NotificationStatus.COMPLETED);

        // Then
        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.COMPLETED);
    }

    @Test
    @DisplayName("성공: 모든 활성화된 유저에게 알림 전송")
    void sendNotifications_AllUsersNotified() {
        // Given
        Product product = Product.builder().id(1L).stock(10).build();
        List<ProductUserNotification> users = Arrays.asList(
                ProductUserNotification.builder().id(1L).userId(1L).build(),
                ProductUserNotification.builder().id(2L).userId(2L).build()
        );
        ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder().build();

        // When
        restockNotificationService.sendNotifications(product, users, notificationHistory);

        // Then
        verify(productUserNotificationHistoryRepository, times(2)).save(any(ProductUserNotificationHistory.class));
    }

    @Test
    @DisplayName("성공: 알림이 중간에 중단된다 (품절 발생)")
    void sendNotifications_StopsWhenOutOfStock() {
        // Given
        Product product = Product.builder().id(1L).stock(1).build();
        List<ProductUserNotification> users = Arrays.asList(
                ProductUserNotification.builder().id(1L).userId(1L).build(),
                ProductUserNotification.builder().id(2L).userId(2L).build()
        );
        ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder().build();

        // When
        restockNotificationService.sendNotifications(product, users, notificationHistory);

        // Then
        verify(productUserNotificationHistoryRepository, times(1)).save(any(ProductUserNotificationHistory.class));
    }

    @Test
    @DisplayName("성공: 알림 이력을 가져온다.")
    void getLatestNotificationHistory_Success() {
        // Given
        Long productId = 1L;
        ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder().build();
        when(productNotificationHistoryRepository.findTopByProductIdOrderByCreatedAtDesc(productId))
                .thenReturn(Optional.of(notificationHistory));

        // When
        ProductNotificationHistory result = restockNotificationService.getLatestNotificationHistory(productId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("실패: 알림 이력을 가져오지 못할 때 예외 발생")
    void getLatestNotificationHistory_Fail() {
        // Given
        Long productId = 1L;
        when(productNotificationHistoryRepository.findTopByProductIdOrderByCreatedAtDesc(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestockException.class,
                () -> restockNotificationService.findProductById(1L));
    }

    @Test
    @DisplayName("성공: 모든 활성화된 유저를 가져온다.")
    void findActiveUserNotifications_Success() {
        // Given
        Long productId = 1L;
        List<ProductUserNotification> users = Arrays.asList(
                ProductUserNotification.builder().id(1L).build(),
                ProductUserNotification.builder().id(2L).build()
        );
        when(productUserNotificationRepository.findByProductIdAndIsActiveTrue(productId))
                .thenReturn(users);

        // When
        List<ProductUserNotification> result = restockNotificationService.findActiveUserNotifications(productId);

        // Then
        assertThat(result).hasSize(2);
    }
}
