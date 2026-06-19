package com.deliveryinsider.domain.auth.responses;

import java.time.LocalDateTime;

public record AuthMeResponse(
        Long id,
        String email,
        LocalDateTime createdAt
) {
}
