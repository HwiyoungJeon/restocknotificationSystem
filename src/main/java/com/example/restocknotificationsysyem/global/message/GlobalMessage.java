package com.example.restocknotificationsysyem.global.message;

public class GlobalMessage {
    public static final String SUCCESS = "정상적으로 실행 되었습니다.";
    public static final String SCHEDULER_RUNNING = "스케줄러 실행 중: 상품 재고 확인 중...";
    public static final String RESTOCK_OCCURED = "상품 ID %d 에 재입고 발생. 알림 전송 로직 실행.";
    public static final String NOTIFICATION_HISTORY_NULL = "알림 이력이 존재하지 않습니다..";
    public static final String NOTIFICATION_SEND_EXCEPTION = "알림 발송 중 예외 발생.";
    public static final String USER_NOTIFICATION_COMPLETED = "유저 %d 에게 알림 전송 완료.";
    public static final String PRODUCT_NOT_FOUND = "상품을 찾을 수 없습니다. ID: %d";
    public static final String NOTIFICATION_HISTORY_NOT_FOUND = "알림 이력이 존재하지 않습니다. Product ID: %d";

    /**
     * RESTOCK_OCCURED 메시지의 productId를 동적으로 추가하는 메서드
     *
     * @param productId 재입고된 상품의 ID
     * @return productId가 포함된 최종 메시지
     */
    public static String getRestockOccuredMessage(Long productId) {
        return String.format(RESTOCK_OCCURED, productId);
    }

    /**
     * USER_NOTIFICATION_COMPLETED 메시지의 userId를 동적으로 추가하는 메서드
     *
     * @param userId 알림을 받은 유저의 ID
     * @return userId가 포함된 최종 메시지
     */
    public static String getUserNotificationCompletedMessage(Long userId) {
        return String.format(USER_NOTIFICATION_COMPLETED, userId);
    }

    /**
     * PRODUCT_NOT_FOUND 메시지의 productId를 동적으로 추가하는 메서드
     *
     * @param productId 찾을 수 없는 상품의 ID
     * @return productId가 포함된 최종 메시지
     */
    public static String getProductNotFoundMessage(Long productId) {
        return String.format(PRODUCT_NOT_FOUND, productId);
    }

    /**
     * NOTIFICATION_HISTORY_NOT_FOUND 메시지의 productId를 동적으로 추가하는 메서드
     *
     * @param productId 알림 이력이 존재하지 않는 상품의 ID
     * @return productId가 포함된 최종 메시지
     */
    public static String getNotificationHistoryNotFoundMessage(Long productId) {
        return String.format(NOTIFICATION_HISTORY_NOT_FOUND, productId);
    }
}
