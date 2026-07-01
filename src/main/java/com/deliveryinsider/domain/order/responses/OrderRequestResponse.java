package com.deliveryinsider.domain.order.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderRequestResponse(

    Long id,
    String requestText,
    String riskType,
    String riskLevel,
    String detectedKeywords,
    String analysisMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {
}
