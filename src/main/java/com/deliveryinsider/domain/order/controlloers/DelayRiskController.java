package com.deliveryinsider.domain.order.controlloers;

import com.deliveryinsider.domain.order.responses.CookingDelayResponse;
import com.deliveryinsider.domain.order.responses.CookingDelayResponse;
import com.deliveryinsider.domain.order.services.DelayRiskService;
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
@RequestMapping("/api/orders")
public class DelayRiskController {

    private final DelayRiskService delayRiskService;

    /**
     * 현재 조리중인 주문의 지연 위험 조회
     *
     * GET /api/orders/delay-risks
     */
    @GetMapping("/delay-risks")
    public ResponseEntity<GlobalRes<List<CookingDelayResponse>>>
    findDelayRisks(
        Authentication authentication
    ) {
        /*
         * JWT 인증 필터가 SecurityContext에 저장한
         * Claims에서 로그인 사용자 PK를 추출한다.
         */
        Long userId = extractUserId(authentication);

        /*
         * 로그인 사용자의 활성 매장과
         * COOKING 주문을 기준으로 지연 위험을 계산한다.
         */
        List<CookingDelayResponse> result =
            delayRiskService.findDelayRisks(userId);

        /*
         * 프로젝트 공통 응답 형식으로 감싸서 반환한다.
         */
        return ResponseEntity.ok(
            GlobalRes.<List<CookingDelayResponse>>builder()
                .code("00")
                .message("조리 지연 위험 조회 성공")
                .data(result)
                .build()
        );
    }

    /**
     * JWT Claims의 subject에서 로그인 회원 PK 추출
     */
    private Long extractUserId(
        Authentication authentication
    ) {
        Claims claims =
            (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}
