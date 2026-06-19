package com.deliveryinsider.domain.auth.responses;

import lombok.Builder;

@Builder
public record RegisterResponse(
        Long id,
        String email
) {
}
