package com.deliveryinsider.domain.report.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 취소 유형별 요약 SQL 결과 Projection
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCancellationSummaryProjection {

    private String cancelType;

    private Long cancelCount;
}