package com.deliveryinsider.domain.report.responses;

import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 운영 리포트 주문 목록 응답 DTO
 */
@Builder
public record ReportOrderResponse(

    Long id,

    String orderNo,

    String platformOrderNumber,

    PlatformType platformType,

    OrderStatus orderStatus,

    String menuSummary,

    Integer totalQuantity,

    String deliveryAddress,

    Integer totalAmount,

    Integer commissionAmount,

    Integer deliveryFee,

    Integer couponCost,

    Integer platformSupportAmount,

    Integer totalMenuCost,

    Integer totalPackagingFee,

    Integer netProfit,

    LocalDateTime orderedAt,

    LocalDateTime cookingStartedAt,

    LocalDateTime canceledAt,

    LocalDateTime completedAt,

    LocalDateTime refundedAt,

    String requestText,

    String requestRiskType,

    String requestRiskLevel,

    String requestAnalysisMessage,

    String cancelType,

    String cancelReason,

    String refundType,

    String refundReason

) {
}
