package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.global.enums.KitchenLoadLevel;
import lombok.Builder;

/**
 * 주문 운영 화면 상단에 표시할 현재 주문 처리 요약 응답 DTO
 *
 * 완료·취소·환불된 과거 주문이 아니라,
 * 현재 처리 중인 주문과 주방 상태를 중심으로 반환한다.
 */
@Builder
public record OrderOperationSummaryResponse(

    /*
     * 현재 처리 중인 전체 주문 수
     *
     * WAITING + COOKING + DELIVERING
     */
    Long progressOrderCount,

    /*
     * 현재 접수대기 상태인 주문 수
     */
    Long waitingCount,

    /*
     * 현재 조리중 상태인 주문 수
     */
    Long cookingCount,

    /*
     * 현재 배달중 상태인 주문 수
     */
    Long deliveringCount,

    /*
     * 현재 COOKING 주문 중
     * WARNING 또는 DELAYED 상태인 주문 수
     */
    Long delayRiskCount,

    /*
     * 현재 진행 중인 주문들의 예상 순수익 합계
     *
     * WAITING + COOKING + DELIVERING 주문의
     * orders.net_profit 합계
     */
    Long expectedProgressNetProfit,

    /*
     * 매장에 설정된 동시 조리 가능 주문 수
     */
    Integer kitchenCapacity,

    /*
     * 현재 COOKING 주문 수와 kitchenCapacity를
     * 비교해 계산한 주방 부하 단계
     *
     * LOW, NORMAL, HIGH, OVERLOAD
     */
    KitchenLoadLevel kitchenLoadLevel,

    /*
     * 점주에게 보여줄 현재 운영 안내 문구
     */
    String message

) {
}
