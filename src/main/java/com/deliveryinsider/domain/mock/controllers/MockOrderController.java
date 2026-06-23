package com.deliveryinsider.domain.mock.controllers;

import com.deliveryinsider.domain.mock.requests.MockOrderCreateRequest;
import com.deliveryinsider.domain.mock.responses.MockOrderCreateResponse;
import com.deliveryinsider.domain.mock.services.MockOrderService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mock-data/orders")
@RequiredArgsConstructor
public class MockOrderController {
    private final MockOrderService mockOrderService;

    /**
     * 기본 데이터 생성
     * POST /api/mock-data/basic
     */
    @PostMapping("/basic")
    public ResponseEntity<GlobalRes<Void>> createBasicData(
            Authentication authentication
    ) {

        Long userId = extractUserId(authentication);

        mockOrderService.createBasicData(userId);

        return ResponseEntity.ok(
                GlobalRes.<Void>builder()
                        .code("00")
                        .message("기본 데이터 생성 성공")
                        .build()
        );
    }

    /**
     * 일반 Mock 주문 생성
     */
    @PostMapping
    public ResponseEntity<GlobalRes<MockOrderCreateResponse>> create(
            @RequestBody MockOrderCreateRequest request,
            Authentication authentication
    ) {

        Long userId = extractUserId(authentication);

        MockOrderCreateResponse result =
                mockOrderService.createMockOrders(userId, request);

        return ResponseEntity.ok(
                GlobalRes.<MockOrderCreateResponse>builder()
                        .code("00")
                        .message("Mock 주문 생성 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 발표용 GROUP Mock 주문
     * TODO 구현 예정
     */
    @PostMapping("/group")
    public ResponseEntity<Void> createGroup() {
        return ResponseEntity.ok().build();
    }

    /**
     * 발표용 PREMIUM Mock 주문
     * TODO 구현 예정
     */
    @PostMapping("/premium")
    public ResponseEntity<Void> createPremium() {
        return ResponseEntity.ok().build();
    }

    /**
     * DELAY_TEST Mock 주문
     * TODO 구현 예정
     */
    @PostMapping("/delay-test")
    public ResponseEntity<Void> createDelayTest() {
        return ResponseEntity.ok().build();
    }

    /**
     * Mock 주문 삭제
     */
    @DeleteMapping
    public ResponseEntity<GlobalRes<Void>> delete(
            Authentication authentication
    ) {

        Long userId = extractUserId(authentication);

        mockOrderService.deleteMockOrders(userId);

        return ResponseEntity.ok(
                GlobalRes.<Void>builder()
                        .code("00")
                        .message("Mock 주문 삭제 성공")
                        .build()
        );
    }

    /**
     * JWT에서 userId 추출
     */
    private Long extractUserId(Authentication authentication) {
        Claims claims = (Claims) authentication.getPrincipal();
        return Long.valueOf(claims.getSubject());
    }


}
