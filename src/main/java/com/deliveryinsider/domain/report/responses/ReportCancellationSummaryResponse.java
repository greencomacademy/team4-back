package com.deliveryinsider.domain.report.responses;

import lombok.Builder;

/**
 * 취소 유형별 요약 응답 DTO
 */
@Builder
public record ReportCancellationSummaryResponse(

    String cancelType,

    Long cancelCount

) {
}
