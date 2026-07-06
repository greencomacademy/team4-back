package com.deliveryinsider.domain.order.entities;

import com.deliveryinsider.domain.order.enums.OrderCancelType;
import com.deliveryinsider.domain.order.enums.OrderCanceledByType;
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
public class OrderCancellation {

    // order_cancellations.id
    private Long id;

    // 연결 주문 번호
    private Long orderId;

    // 취소 유형
    private OrderCancelType cancelType;

    // 상세 취소 사유
    private String cancelReason;

    // 취소 전 주문 상태
    private OrderStatus previousStatus;

    // 취소 처리자 유형
    private OrderCanceledByType canceledByType;

    // 취소 처리 회원 번호
    private Long canceledByUserId;

    // 취소 처리 시각
    private LocalDateTime canceledAt;

    // DB 생성 시각
    private LocalDateTime createdAt;
}
