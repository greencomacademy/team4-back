package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderListResponse(

    Long id,
    String orderNo,
    String platformOrderNumber,
    PlatformType platformType,
    OrderStatus orderStatus,

    Integer totalAmount,
    Integer netProfit,
    Integer totalCookingTime,

    Integer totalQuantity,
    String menuSummary,

    String deliveryAddress,

    LocalDateTime orderedAt,
    LocalDateTime cookingStartedAt,

    String requestText,
    String requestRiskType,
    String requestRiskLevel,
    String requestAnalysisMessage,

    String cancelType,
    String cancelReason,
    LocalDateTime canceledAt

) {
}
