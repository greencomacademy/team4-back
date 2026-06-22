package com.deliveryinsider.domain.order.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 현재 진행 중인 주문의 상태별 개수와
 * 예상 순수익 집계 SQL 결과를 받는 Projection
 *
 * 지연 위험 건수, 주방 부하 단계, 안내 문구는
 * Service에서 계산하므로 이 Projection에는 포함하지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderOperationSummaryProjection {

    /*
     * 현재 처리 중인 전체 주문 수
     *
     * WAITING + COOKING + DELIVERING
     */
    private Long progressOrderCount;

    /*
     * 현재 WAITING 상태인 주문 수
     */
    private Long waitingCount;

    /*
     * 현재 COOKING 상태인 주문 수
     */
    private Long cookingCount;

    /*
     * 현재 DELIVERING 상태인 주문 수
     */
    private Long deliveringCount;

    /*
     * 현재 진행 중인 주문들의 예상 순수익 합계
     *
     * WAITING + COOKING + DELIVERING 주문의
     * net_profit 합계
     */
    private Long expectedProgressNetProfit;
}
