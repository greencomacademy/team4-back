package com.deliveryinsider.domain.report.projections;

import com.deliveryinsider.global.enums.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 플랫폼별 리포트 SQL 결과 Projection
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportPlatformProjection {

    private PlatformType platformType;

    private Long totalOrderCount;

    private Long completedOrderCount;

    private Long canceledOrderCount;

    private Long totalSales;

    private Long totalNetProfit;
}
