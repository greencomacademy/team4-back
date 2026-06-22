package com.deliveryinsider.domain.orders.controlloers;


import com.deliveryinsider.domain.orders.responses.OrderListResponse;
import com.deliveryinsider.domain.orders.services.OrderService;
import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.responses.GlobalRes;

import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    /**
     * 주문 목록 조회
     * GET /api/orders
     * GET /api/orders?platformType=BAEMIN
     * GET /api/orders?orderStatus=WAITING
     * GET /api/orders?platformType=BAEMIN&orderStatus=WAITING
     */
    @GetMapping()
    public ResponseEntity<GlobalRes<List<OrderListResponse>>> findAll(
        Authentication authentication,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        OrderStatus orderStatus
    ){
        Long userId = extractUserId(authentication);

        List<OrderListResponse> result =
            orderService.findAllOrders(
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
     * JWT subject에서 로그인 회원 PK 추출 - 공통메서드로 사용
     */
    private Long extractUserId(
        Authentication authentication
    ) {
        Claims claims =
            (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}
