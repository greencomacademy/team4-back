package com.deliveryinsider.domain.mock.controllers;

import com.deliveryinsider.domain.mock.requests.MockOrderCreateRequest;
import com.deliveryinsider.domain.mock.responses.MockOrderCreateResponse;
import com.deliveryinsider.domain.mock.responses.MockOrderDeleteResponse;
import com.deliveryinsider.domain.mock.services.MockOrderService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mock-data/orders")
@RequiredArgsConstructor
public class MockOrderController {

    private final MockOrderService mockOrderService;
    // TODO: Mock 대량 주문의 totalCookingTime이 과도하게 커지는 문제를 실제 주방의 병렬 조리 방식을 고려하여 현실적으로 조정한다.
    /**
     * Mock 주문 생성
     * POST /api/orders/mock
     */
    @PostMapping
    public ResponseEntity<GlobalRes<MockOrderCreateResponse>> createMockOrders(
            Authentication authentication,
            @Valid @RequestBody MockOrderCreateRequest createReq
    ) {
        Long userId = extractUserId(authentication);

        MockOrderCreateResponse result =
                mockOrderService.create(userId, createReq);

        return ResponseEntity.ok(
                GlobalRes.<MockOrderCreateResponse>builder()
                        .code("00")
                        .message("Mock 주문 생성 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 로그인 사용자의 매장에 속한 Mock 주문 전체 삭제
     *
     * DELETE /api/mock-data/orders
     */
    @DeleteMapping
    public ResponseEntity<GlobalRes<MockOrderDeleteResponse>> deleteAll(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        MockOrderDeleteResponse result =
                mockOrderService.deleteAll(userId);

        return ResponseEntity.ok(
                GlobalRes.<MockOrderDeleteResponse>builder()
                        .code("00")
                        .message("Mock 주문 삭제 완료")
                        .data(result)
                        .build()
        );
    }


    /**
     * JWT subject에서 로그인 회원 PK 추출
     */
    private Long extractUserId(
            Authentication authentication
    ) {
        Claims claims =
                (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}
