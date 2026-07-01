package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.enums.OrderStatus;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDetailResponse(

    Long id,
    String orderNo,
    String platformOrderNumber,

    PlatformType platformType,
    OrderStatus orderStatus,

    Integer totalAmount,
    Integer commissionAmount,
    Integer couponCost,
    Integer deliveryFee,
    Integer platformSupportAmount,
    Integer totalMenuCost,
    Integer totalPackagingFee,
    Integer netProfit,

    Integer totalCookingTime,
    String deliveryAddress,

    LocalDateTime orderedAt,
    LocalDateTime cookingStartedAt,
    LocalDateTime completedAt,
    LocalDateTime canceledAt,
    LocalDateTime refundedAt,

    List<OrderItemResponse> items,

    OrderRequestResponse request,
    OrderCancellationResponse cancellation,
    OrderRefundResponse refund,
    
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {
}