package com.deliveryinsider.domain.report.responses;

import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

/**
 * 플랫폼별 리포트 응답 DTO
 */
@Builder
public record ReportPlatformResponse(

    PlatformType platformType,

    Long totalOrderCount,

    Long completedOrderCount,

    Long canceledOrderCount,

    Long totalSales,

    Long totalNetProfit,

    Double cancelRate,

    Long averageOrderAmount,

    Long averageNetProfit

) {
}
