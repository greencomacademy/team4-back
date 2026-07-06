package com.deliveryinsider.domain.menus.entities;

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
public class MenuLossDismissal {

    private Long id;

    private Long storeId;

    private Long menuId;

    private LocalDateTime dismissedAt;

    private LocalDateTime hideUntil;

    private LocalDateTime restoredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
