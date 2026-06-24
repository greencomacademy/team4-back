package com.deliveryinsider.domain.menus.responses;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MenuResponse(

        Long id,

        String menuName,

        Integer menuPrice,

        Integer menuCost,

        Integer packagingFee,

        Integer expectedCookingTime,

        Integer batchCapacity,

        // 판매가 - 원가 - 포장비
        Integer expectedMargin,

        // 예상 마진 ÷ 판매가 × 100
        BigDecimal expectedMarginRate,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}