package com.deliveryinsider.domain.auth.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReissueTokenResponse(
        String accessToken
) {
}
