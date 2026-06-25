package com.deliveryinsider.domain.report.responses;

import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 취소 리포트 목록 응답 DTO
 */
@Builder
public record ReportCancellationResponse(

    Long id,

    String orderNo,

    String platformOrderNumber,

    PlatformType platformType,

    OrderStatus previousStatus,

    String menuSummary,

    Integer totalQuantity,

    Integer totalAmount,

    Integer netProfit,

    String cancelType,

    String cancelReason,

    String canceledByType,

    Long canceledByUserId,

    LocalDateTime orderedAt,

    LocalDateTime canceledAt,

    String requestText,

    String requestRiskType,

    String requestRiskLevel

) {
}