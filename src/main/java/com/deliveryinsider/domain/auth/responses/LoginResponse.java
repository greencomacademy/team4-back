package com.deliveryinsider.domain.auth.responses;

import com.deliveryinsider.domain.user.responses.UserResponse;
import lombok.Builder;

@Builder
public record LoginResponse(
    String accessToken,
    UserResponse user
) {
}
