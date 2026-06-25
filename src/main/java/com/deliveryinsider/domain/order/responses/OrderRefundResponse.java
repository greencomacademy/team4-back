package com.deliveryinsider.domain.order.responses;

import com.deliveryinsider.domain.order.enums.OrderRefundType;
import com.deliveryinsider.domain.order.enums.OrderRefundedByType;
import com.deliveryinsider.global.enums.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderRefundResponse(

    Long id,

    OrderRefundType refundType,

    String refundReason,

    OrderStatus previousStatus,

    OrderRefundedByType refundedByType,

    Long refundedByUserId,

    LocalDateTime refundedAt,

    LocalDateTime createdAt

) {
}
