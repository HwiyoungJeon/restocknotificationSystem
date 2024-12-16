package com.example.restocknotificationsysyem.global.scheduler;

import com.example.restocknotificationsysyem.domain.service.RestockNotificationService;
import com.example.restocknotificationsysyem.global.message.GlobalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestockScheduler {

    private final RestockNotificationService restockNotificationService;

    /**
     * 1분마다 재고 확인 후 재입고 발생 시 자동으로 알림 발송
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkRestock() {
        System.out.println(GlobalMessage.SCHEDULER_RUNNING);

        // 특정 조건에 따라 재입고를 감지 (예시: 모든 상품의 재고 확인)
        for (Long productId : restockNotificationService.findAllProductIds()) {
            boolean isRestocked = restockNotificationService.checkForRestock(productId);
            if (isRestocked) {
                System.out.println(GlobalMessage.getRestockOccuredMessage(productId));
                restockNotificationService.restockProduct(productId);
            }
        }
    }
}