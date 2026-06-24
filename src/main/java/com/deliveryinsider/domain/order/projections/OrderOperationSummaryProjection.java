package com.deliveryinsider.domain.order.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 실시간 운영 대시보드 요약 SQL 결과 Projection
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderOperationSummaryProjection {

    private Long todayOrderCount;

    private Long progressOrderCount;
    private Long waitingCount;
    private Long cookingCount;
    private Long deliveringCount;

    private Long completedCount;
    private Long canceledCount;

    private Long todaySales;
    private Long todayNetProfit;

    private Long expectedProgressNetProfit;

    private Long requestRiskCount;
    private Long lossRiskCount;
}