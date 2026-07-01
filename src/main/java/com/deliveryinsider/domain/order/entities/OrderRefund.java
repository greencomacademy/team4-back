package com.deliveryinsider.domain.order.entities;

import com.deliveryinsider.domain.order.enums.OrderRefundType;
import com.deliveryinsider.domain.order.enums.OrderRefundedByType;
import com.deliveryinsider.global.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefund {

    private Long id;

    private Long orderId;

    private OrderRefundType refundType;

    private String refundReason;

    private OrderStatus previousStatus;

    private OrderRefundedByType refundedByType;

    private Long refundedByUserId;

    private LocalDateTime refundedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}