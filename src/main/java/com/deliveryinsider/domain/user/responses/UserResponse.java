package com.deliveryinsider.domain.user.responses;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email
) {
}
