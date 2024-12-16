package com.example.restocknotificationsysyem.domain.controller;

import com.example.restocknotificationsysyem.domain.dto.RestockResponseDto;
import com.example.restocknotificationsysyem.domain.service.RestockNotificationService;
import com.example.restocknotificationsysyem.global.exception.SuccessResponse;
import com.example.restocknotificationsysyem.global.message.GlobalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RestockNotificationController {

    private final RestockNotificationService restockNotificationService;

    /**
     * 재입고 및 알림 전송 API
     *
     * @param productId 상품 ID
     */
    @PostMapping("/products/{productId}/notifications/re-stock")
    public ResponseEntity<SuccessResponse<RestockResponseDto>> restockProduct(@PathVariable Long productId) {
        RestockResponseDto responseDto = restockNotificationService.restockProduct(productId);
        SuccessResponse<RestockResponseDto> successResponse = new SuccessResponse<>(
                GlobalMessage.SUCCESS,
                HttpStatus.OK.value(),
                responseDto
        );

        return ResponseEntity.ok(successResponse);
    }

    /**
     * 재발송 요청 API (마지막 전송 유저 이후부터 다시 알림 전송)
     *
     * @param productId 상품 ID
     */
    @PostMapping("/admin/products/{productId}/notifications/re-stock")
    public ResponseEntity<SuccessResponse<RestockResponseDto>> retryNotification(@PathVariable Long productId) {
        RestockResponseDto responseDto = restockNotificationService.retryNotification(productId);
        SuccessResponse<RestockResponseDto> successResponse = new SuccessResponse<>(
                GlobalMessage.SUCCESS,
                HttpStatus.OK.value(),
                responseDto
        );

        return ResponseEntity.ok(successResponse);
    }
}