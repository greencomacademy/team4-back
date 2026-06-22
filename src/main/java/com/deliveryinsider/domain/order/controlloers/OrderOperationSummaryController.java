package com.deliveryinsider.domain.order.controlloers;

import com.deliveryinsider.domain.order.responses.OrderOperationSummaryResponse;
import com.deliveryinsider.domain.order.services.OrderOperationSummaryService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderOperationSummaryController {

    private final OrderOperationSummaryService
        orderOperationSummaryService;

    /**
     * 로그인 사용자의 현재 주문 운영 요약을 조회한다.
     * GET /api/orders/operation-summary
     */
    @GetMapping("/operation-summary")
    public ResponseEntity<
        GlobalRes<OrderOperationSummaryResponse>
        > findOperationSummary(
        Authentication authentication
    ) {
        /*
         * JWT 인증 필터가 SecurityContext에 저장한
         * Claims에서 로그인 회원 PK를 추출한다.
         */
        Long userId =
            extractUserId(authentication);

        /*
         * 진행 주문 집계, 지연 위험 계산,
         * 주방 부하 판단과 안내 문구 생성은
         * OrderOperationSummaryService가 담당한다.
         */
        OrderOperationSummaryResponse result =
            orderOperationSummaryService
                .findOperationSummary(userId);

        /*
         * 프로젝트 공통 응답 형식으로 감싸서 반환한다.
         */
        return ResponseEntity.ok(
            GlobalRes
                .<OrderOperationSummaryResponse>builder()
                .code("00")
                .message("주문 운영 요약 조회 성공")
                .data(result)
                .build()
        );
    }

    /**
     * JWT Claims의 subject에서 로그인 회원 PK를 추출한다.
     */
    private Long extractUserId(
        Authentication authentication
    ) {
        Claims claims =
            (Claims) authentication.getPrincipal();

        return Long.valueOf(
            claims.getSubject()
        );
    }
}
