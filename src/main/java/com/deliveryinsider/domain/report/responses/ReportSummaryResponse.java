package com.deliveryinsider.domain.report.responses;

import lombok.Builder;

/**
 * 운영 리포트 요약 응답 DTO
 */
@Builder
public record ReportSummaryResponse(

    Long totalOrderCount,

    Long completedOrderCount,

    Long canceledOrderCount,

    Long totalSales,

    Long totalNetProfit,

    Double cancelRate,

    Long averageOrderAmount,

    Long requestRiskCount,

    Long lossRiskCount

) {
}