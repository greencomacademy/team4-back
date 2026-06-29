package com.deliveryinsider.domain.menus.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MenuLossDismissalResponse(

        Long id,

        Long menuId,

        LocalDateTime dismissedAt,

        LocalDateTime hideUntil

) {
}
