package com.deliveryinsider.domain.notification.controllers;

import com.deliveryinsider.domain.notification.responses.HeaderNotificationResponse;
import com.deliveryinsider.domain.notification.services.NotificationService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 헤더 알림 조회 API
     * - 하드코딩 알림 대신 현재 운영 요약 기준으로 생성된 알림을 반환한다.
     */
    @GetMapping("/header")
    public ResponseEntity<GlobalRes<List<HeaderNotificationResponse>>> findHeaderNotifications(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<HeaderNotificationResponse> result =
                notificationService.findHeaderNotifications(userId);

        return ResponseEntity.ok(GlobalRes.<List<HeaderNotificationResponse>>builder()
                .code("00")
                .message("헤더 알림 조회 성공")
                .data(result)
                .build());
    }

    private Long extractUserId(Authentication authentication) {
        Claims claims = (Claims) authentication.getPrincipal();
        return Long.valueOf(claims.getSubject());
    }
}
