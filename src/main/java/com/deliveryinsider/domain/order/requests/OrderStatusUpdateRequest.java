package com.deliveryinsider.domain.order.requests;

import com.deliveryinsider.global.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

    @NotNull(message = "변경할 주문 상태는 필수입니다.")
    OrderStatus orderStatus

) {
}
