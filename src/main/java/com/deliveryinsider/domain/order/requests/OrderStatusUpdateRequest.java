package com.deliveryinsider.domain.order.requests;

import com.deliveryinsider.domain.order.enums.OrderCancelType;
import com.deliveryinsider.domain.order.enums.OrderRefundType;
import com.deliveryinsider.global.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusUpdateRequest(

    @NotNull(message = "변경할 주문 상태는 필수입니다.")
    OrderStatus orderStatus,

    // orderStatus가 CANCELED일 때만 필수로 검사한다.
    OrderCancelType cancelType,

    @Size(max = 1000, message = "취소 사유는 1000자 이하로 입력해 주세요.")
    String cancelReason,

    // orderStatus가 REFUNDED일 때만 필수로 검사한다.
    OrderRefundType refundType,

    @Size(max = 1000, message = "환불 사유는 1000자 이하로 입력해 주세요.")
    String refundReason

) {
}
