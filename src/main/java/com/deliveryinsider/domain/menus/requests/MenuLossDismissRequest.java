package com.deliveryinsider.domain.menus.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MenuLossDismissRequest(

        @Min(value = 1, message = "숨김 기간은 1일 이상이어야 합니다.")
        @Max(value = 30, message = "숨김 기간은 최대 30일까지만 가능합니다.")
        Integer hideDays

) {
}
