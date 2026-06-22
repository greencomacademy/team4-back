package com.deliveryinsider.domain.order.controlloers;

import com.deliveryinsider.domain.order.requests.OrderStatusUpdateRequest;
import com.deliveryinsider.domain.order.responses.OrderDetailResponse;
import com.deliveryinsider.domain.order.responses.OrderListResponse;
import com.deliveryinsider.domain.order.services.OrderService;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.enums.OrderStatus;

import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 목록 조회
     * GET /api/orders
     * GET /api/orders?platformType=BAEMIN
     * GET /api/orders?orderStatus=WAITING
     * GET /api/orders?platformType=BAEMIN&orderStatus=WAITING
     */
    @GetMapping
    public ResponseEntity<GlobalRes<List<OrderListResponse>>> findAll(
        Authentication authentication,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        OrderStatus orderStatus
    ) {
        Long userId = extractUserId(authentication);

        List<OrderListResponse> result =
            orderService.findAll(
                userId,
                platformType,
                orderStatus
            );

        return ResponseEntity.ok(
            GlobalRes.<List<OrderListResponse>>builder()
                .code("00")
                .message("주문 목록 조회 성공")
                .data(result)
                .build()
        );
    }

    /**
     * 주문 상세 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<GlobalRes<OrderDetailResponse>> findOne(
        Authentication authentication,
        @PathVariable Long orderId
    ) {
        Long userId = extractUserId(authentication);

        OrderDetailResponse result =
            orderService.findOne(userId, orderId);

        return ResponseEntity.ok(
            GlobalRes.<OrderDetailResponse>builder()
                .code("00")
                .message("주문 상세 조회 성공")
                .data(result)
                .build()
        );
    }
    /**
     * 주문 상태 변경
     * PATCH /api/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<GlobalRes<OrderDetailResponse>> updateStatus(
        Authentication authentication,
        @PathVariable Long orderId,
        @Valid @RequestBody OrderStatusUpdateRequest updateReq
    ) {
        Long userId = extractUserId(authentication);

        OrderDetailResponse result =
            orderService.updateStatus(
                userId,
                orderId,
                updateReq
             );

        return ResponseEntity.ok(
            GlobalRes.<OrderDetailResponse>builder()
                .code("00")
                .message("주문 상태 변경 성공")
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
