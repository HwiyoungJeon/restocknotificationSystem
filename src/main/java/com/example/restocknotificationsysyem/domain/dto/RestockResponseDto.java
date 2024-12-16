package com.example.restocknotificationsysyem.domain.dto;

import com.example.restocknotificationsysyem.global.status.NotificationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RestockResponseDto {

    // 상품 정보
    private Long productId;                      // 상품 ID
    private int stock;                          // 남은 재고
    private int restockRound;                  // 재입고 회차
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime restockTime;         // 재입고된 시간

    // 알림 정보
    private Long notificationHistoryId;        // 알림 히스토리 ID
    private NotificationStatus status;         // 알림 상태 (IN_PROGRESS, CANCELED_BY_SOLD_OUT, CANCELED_BY_ERROR, COMPLETED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notificationStartTime; // 알림 시작 시간
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notificationEndTime;   // 알림 종료 시간
    private Long lastSentUserId;               // 마지막으로 알림을 전송한 사용자 ID

    // 알림 사용자 정보
    private List<UserNotificationInfo> notifiedUsers; // 알림을 받은 사용자 정보 목록

    @Getter
    @Builder
    public static class UserNotificationInfo {
        private Long userId;                     // 사용자 ID
        private boolean notificationSent;        // 알림 전송 여부 (성공/실패)
    }
}