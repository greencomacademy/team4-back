package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.domain.order.enums.DelayRiskLevel;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.enums.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 조리중 주문의 지연 위험 계산 결과 응답 DTO
 *
 * DB에 저장된 주문 정보와
 * DelayRiskService에서 실시간으로 계산한 값을
 * 프론트엔드에 전달한다.
 */
@Builder
public record CookingDelayResponse(

    // 주문 PK
    Long id,

    // 화면에 표시할 주문번호
    String orderNo,

    // 주문이 들어온 배달 플랫폼
    PlatformType platformType,

    // 현재 주문 상태
    // 이 API에서는 기본적으로 COOKING만 반환한다.
    OrderStatus orderStatus,

    // 주문에 포함된 전체 메뉴 수량
    Integer totalQuantity,

    // 예: 묵은지 김치찜 외 2개
    String menuSummary,

    // 주문 생성 당시 계산해 저장한 전체 예상 조리시간
    Integer totalCookingTime,

    // WAITING에서 COOKING으로 변경된 시각
    LocalDateTime cookingStartedAt,

    // 조리 시작 후 현재까지 지난 시간(분)
    Long elapsedMinutes,

    // 현재 매장에서 조리중인 전체 주문 수
    Integer currentCookingOrderCount,

    // 매장의 동시 조리 처리 가능 수
    Integer kitchenCapacity,

    // ceil(현재 조리중 주문 수 / 주방 처리량)
    Integer loadMultiplier,

    // totalCookingTime × loadMultiplier
    Integer adjustedCookingTime,

    // 경과시간 / 조정 예상 조리시간 × 100
    BigDecimal progressRate,

    // SAFE, WARNING, DELAYED
    DelayRiskLevel delayRiskLevel

) {
}