package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.global.enums.KitchenLoadLevel;
import lombok.Builder;

/**
 * 실시간 운영 대시보드 상단 요약 응답 DTO
 */
@Builder
public record OrderOperationSummaryResponse(

    // 오늘 전체 주문 수
    Long todayOrderCount,

    // 현재 처리 중 주문 수: WAITING + COOKING + DELIVERING
    Long progressOrderCount,

    Long waitingCount,
    Long cookingCount,
    Long deliveringCount,

    // 오늘 완료/취소 주문 수
    Long completedCount,
    Long canceledCount,

    // 오늘 완료 주문 기준 매출/순수익
    Long todaySales,
    Long todayNetProfit,

    // 오늘 취소율
    Double cancelRate,

    // 지연 위험 주문 수
    Long delayRiskCount,

    // 요구사항 확인 필요 주문 수
    Long requestRiskCount,

    // 예상 순수익이 0 이하인 주문 수
    Long lossRiskCount,

    // 현재 진행 주문 예상 순수익 합계
    Long expectedProgressNetProfit,

    // 매장 주방 처리량
    Integer kitchenCapacity,

    // 부하율
    Integer loadRate,

    // 부하 단계
    KitchenLoadLevel kitchenLoadLevel,

    // 점주 안내 문구
    String message

) {
}
