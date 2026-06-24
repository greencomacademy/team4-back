package com.deliveryinsider.domain.menus.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuCreateRequest(

        @NotBlank(message = "메뉴명은 필수입니다.")
        @Size(
                max = 100,
                message = "메뉴명은 100자 이하여야 합니다."
        )
        String menuName,

        @NotNull(message = "판매가는 필수입니다.")
        @Min(
                value = 0,
                message = "판매가는 0원 이상이어야 합니다."
        )
        Integer menuPrice,

        @NotNull(message = "원가는 필수입니다.")
        @Min(
                value = 0,
                message = "원가는 0원 이상이어야 합니다."
        )
        Integer menuCost,

        @NotNull(message = "포장비는 필수입니다.")
        @Min(
                value = 0,
                message = "포장비는 0원 이상이어야 합니다."
        )
        Integer packagingFee,

        @NotNull(message = "예상 조리시간은 필수입니다.")
        @Min(
                value = 1,
                message = "예상 조리시간은 1분 이상이어야 합니다."
        )
        Integer expectedCookingTime,

        @NotNull(message = "동시 조리 가능 수량은 필수입니다.")
        @Min(
                value = 1,
                message = "동시 조리 가능 수량은 1개 이상이어야 합니다."
        )
        Integer batchCapacity

) {
}