package com.deliveryinsider.domain.platform.responses;

import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PlatformResponse(
        Long id,
        PlatformType platformType,
        BigDecimal commissionRate,
        Integer deliveryFee,
        Integer couponCost,
        Integer platformSupportAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}