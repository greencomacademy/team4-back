package com.deliveryinsider.domain.platform.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record PlatformUpdateRequest(

        @DecimalMin(
                value = "0.00",
                message = "수수료율은 0 이상이어야 합니다."
        )
        @DecimalMax(
                value = "100.00",
                message = "수수료율은 100 이하이어야 합니다."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "수수료율은 소수점 둘째 자리까지 입력할 수 있습니다."
        )
        BigDecimal commissionRate,

        @Min(
                value = 0,
                message = "배달비는 0 이상이어야 합니다."
        )
        Integer deliveryFee,

        @Min(
                value = 0,
                message = "쿠폰 부담금은 0 이상이어야 합니다."
        )
        Integer couponCost,

        @Min(
                value = 0,
                message = "플랫폼 지원금은 0 이상이어야 합니다."
        )
        Integer platformSupportAmount

) {

    @AssertTrue(
            message = "수정할 플랫폼 설정을 하나 이상 입력해야 합니다."
    )
    public boolean isUpdateFieldPresent() {
        return commissionRate != null
                || deliveryFee != null
                || couponCost != null
                || platformSupportAmount != null;
    }
}