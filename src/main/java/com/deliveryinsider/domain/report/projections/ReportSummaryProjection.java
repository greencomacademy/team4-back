package com.deliveryinsider.domain.report.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 운영 리포트 요약 SQL 결과 Projection
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryProjection {

    private Long totalOrderCount;

    private Long completedOrderCount;

    private Long canceledOrderCount;

    private Long totalSales;

    private Long totalNetProfit;

    private Long requestRiskCount;

    private Long lossRiskCount;
}
