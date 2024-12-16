package com.example.restocknotificationsysyem.domain.service;

import com.example.restocknotificationsysyem.domain.dto.RestockResponseDto;
import com.example.restocknotificationsysyem.domain.entity.Product;
import com.example.restocknotificationsysyem.domain.entity.ProductNotificationHistory;
import com.example.restocknotificationsysyem.domain.entity.ProductUserNotification;
import com.example.restocknotificationsysyem.domain.entity.ProductUserNotificationHistory;
import com.example.restocknotificationsysyem.domain.repository.ProductNotificationHistoryRepository;
import com.example.restocknotificationsysyem.domain.repository.ProductRepository;
import com.example.restocknotificationsysyem.domain.repository.ProductUserNotificationHistoryRepository;
import com.example.restocknotificationsysyem.domain.repository.ProductUserNotificationRepository;
import com.example.restocknotificationsysyem.global.exception.RestockException;
import com.example.restocknotificationsysyem.global.message.GlobalMessage;
import com.example.restocknotificationsysyem.global.status.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestockNotificationService {

    private final ProductRepository productRepository;
    private final ProductUserNotificationRepository productUserNotificationRepository;
    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;

    /**
     * 상품 ID 목록 조회
     */
    public List<Long> findAllProductIds() {
        return productRepository.findAllProductIds();
    }

    /**
     * 특정 상품의 재고 변화 확인
     */
    public boolean checkForRestock(Long productId) {
        Product product = findProductById(productId);
        return product.getStock() > product.getPreviousStock();
    }

    /**
     * 상품 재입고
     */
    @Transactional
    public RestockResponseDto restockProduct(Long productId) {
        Product product = findProductById(productId);

        // 🔥 기존 로직 정리 - 재고 감소 및 회차 증가
        product.increaseRestockRound();
        product.updatePreviousStock(product.getStock());

        // 🔥 하나의 트랜잭션으로 관리됨 (save를 따로 호출하지 않음 - Dirty Checking 사용)

        ProductNotificationHistory notificationHistory = createNotificationHistory(product);
        List<ProductUserNotification> users = findActiveUserNotifications(productId);
        sendNotifications(product, users, notificationHistory);

        return buildRestockResponseDto(product, notificationHistory, users);
    }


    /**
     * 알림 재발송
     */
    @Transactional
    public RestockResponseDto retryNotification(Long productId) {
        ProductNotificationHistory notificationHistory = getLatestNotificationHistory(productId);
        List<ProductUserNotification> users = findActiveUserNotifications(productId);
        sendRemainingNotifications(users, notificationHistory.getLastSentUserId(), notificationHistory);
        Product product = findProductById(productId);
        return buildRestockResponseDto(product, notificationHistory, users);
    }

    public void sendNotifications(Product product, List<ProductUserNotification> users, ProductNotificationHistory notificationHistory) {
        if (notificationHistory == null) {
            throw new RestockException(GlobalMessage.NOTIFICATION_HISTORY_NULL);
        }
        for (ProductUserNotification user : users) {
            // 🔥 이미 발송된 유저는 건너뜀 (중복 방지)
            if (notificationHistory.getLastSentUserId() != null &&
                    notificationHistory.getLastSentUserId().equals(user.getUserId())) {
                continue;
            }

            sendNotificationWithTracking(product, user, notificationHistory);
            product.decreaseStock();
            // 🔥 재고가 없으면 중단
            if (product.isOutOfStock()) {
                updateNotificationStatus(notificationHistory, NotificationStatus.CANCELED_BY_SOLD_OUT);
                break;
            }
        }

        // 🔥 품절이 발생하지 않으면 상태를 COMPLETED로 변경
        if (!product.isOutOfStock()) {
            updateNotificationStatus(notificationHistory, NotificationStatus.COMPLETED);
        }
    }

    public void sendRemainingNotifications(List<ProductUserNotification> users, Long lastSentUserId, ProductNotificationHistory notificationHistory) {
        boolean startSending = (lastSentUserId == null);

        for (ProductUserNotification user : users) {
            if (!startSending && user.getUserId().equals(lastSentUserId)) {
                startSending = true;
                continue;
            }

            if (startSending) {
                sendNotificationWithTracking(null, user, notificationHistory);
                notificationHistory.updateLastSentUserId(user.getUserId());
            }
        }

        updateNotificationStatus(notificationHistory, NotificationStatus.COMPLETED);
    }

    public void sendNotificationWithTracking(Product product, ProductUserNotification user, ProductNotificationHistory notificationHistory) {
        try {
            sendNotification(user);
            if (product != null && product.getStock() > 0) {
                product.decreaseStock();
            }
            trackUserNotificationHistory(user, notificationHistory.getRestockRound(), notificationHistory);
        } catch (Exception e) {
            updateNotificationStatus(notificationHistory, NotificationStatus.CANCELED_BY_ERROR);
            throw new RestockException(GlobalMessage.NOTIFICATION_SEND_EXCEPTION);
        }
    }

    private void sendNotification(ProductUserNotification user) {
        System.out.println(GlobalMessage.getUserNotificationCompletedMessage(user.getId()));
    }

    public void trackUserNotificationHistory(ProductUserNotification user, int restockRound, ProductNotificationHistory notificationHistory) {
        ProductUserNotificationHistory userNotificationHistory = ProductUserNotificationHistory.builder()
                .product(user.getProduct())
                .userNotification(user)
                .restockRound(restockRound)
                .build();

        productUserNotificationHistoryRepository.save(userNotificationHistory);
        notificationHistory.updateLastSentUserId(user.getUserId());
    }


    public void updateNotificationStatus(ProductNotificationHistory notificationHistory, NotificationStatus status) {
        if (notificationHistory != null) {
            notificationHistory.updateStatus(status);
            // 🔥 중복 저장 방지
            productNotificationHistoryRepository.save(notificationHistory);
        }
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RestockException(GlobalMessage.getProductNotFoundMessage(productId)));
    }

    public ProductNotificationHistory createNotificationHistory(Product product) {
        ProductNotificationHistory history = ProductNotificationHistory.builder()
                .product(product)
                .restockRound(product.getRestockRound())
                .status(NotificationStatus.IN_PROGRESS)
                .build();
        return productNotificationHistoryRepository.save(history);
    }

    public ProductNotificationHistory getLatestNotificationHistory(Long productId) {
        return productNotificationHistoryRepository.findTopByProductIdOrderByCreatedAtDesc(productId)
                .orElseThrow(() -> new RestockException(GlobalMessage.getNotificationHistoryNotFoundMessage(productId)));
    }

    public List<ProductUserNotification> findActiveUserNotifications(Long productId) {
        return productUserNotificationRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public RestockResponseDto buildRestockResponseDto(Product product, ProductNotificationHistory notificationHistory, List<ProductUserNotification> users) {
        List<RestockResponseDto.UserNotificationInfo> userNotificationInfos = users.stream()
                .map(user -> RestockResponseDto.UserNotificationInfo.builder()
                        .userId(user.getUserId())
                        .notificationSent(true)
                        .build())
                .collect(Collectors.toList());

        return RestockResponseDto.builder()
                .productId(product.getId())
                .stock(product.getStock())
                .restockRound(product.getRestockRound())
                .restockTime(LocalDateTime.now())
                .notificationHistoryId(notificationHistory.getId())
                .status(notificationHistory.getStatus())
                .notificationStartTime(notificationHistory.getCreatedAt())
                .notificationEndTime(notificationHistory.getModifiedAt())
                .lastSentUserId(notificationHistory.getLastSentUserId())
                .notifiedUsers(userNotificationInfos)
                .build();
    }
}
