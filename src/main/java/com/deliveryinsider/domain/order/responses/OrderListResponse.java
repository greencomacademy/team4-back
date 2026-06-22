package com.deliveryinsider.domain.order.responses;


import com.deliveryinsider.global.enums.OrderStatus;

import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderListResponse(

    Long id,
    String orderNo,
    PlatformType platformType,
    OrderStatus orderStatus,

    Integer totalAmount,
    Integer netProfit,
    Integer totalCookingTime,

    Integer totalQuantity,
    String menuSummary,

    LocalDateTime orderedAt,
    LocalDateTime cookingStartedAt

) {
}
