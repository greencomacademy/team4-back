package com.deliveryinsider.domain.notification.responses;

import lombok.Builder;

@Builder
public record HeaderNotificationResponse(
        String title,
        String description,
        String path
) {
}
