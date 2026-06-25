package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.domain.order.enums.OrderCancelType;
import com.deliveryinsider.domain.order.enums.OrderCanceledByType;
import com.deliveryinsider.global.enums.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderCancellationResponse(

    Long id,
    OrderCancelType cancelType,
    String cancelReason,
    OrderStatus previousStatus,
    OrderCanceledByType canceledByType,
    Long canceledByUserId,
    LocalDateTime canceledAt,
    LocalDateTime createdAt

) {
}
