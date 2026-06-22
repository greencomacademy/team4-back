package com.deliveryinsider.domain.store.requests;

import jakarta.validation.constraints.*;

import java.time.LocalTime;

public record StoreCreateReq(
        @NotBlank(message = "매장명은 필수입니다.")
        @Size(max = 100, message = "매장명은 100자 이하여야 합니다.")
        String storeName,

        //phone
        @NotBlank(message ="가게 전화번호는 필수 입니다")
        @Size(max = 20, message = "가게번호는 20자 이하 여야 합니다.")
        String phone,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
        String addressDetail,

        @NotBlank(message = "업종은 필수입니다.")
        @Size(max = 50, message = "업종은 50자 이하여야 합니다.")
        String industryType,

        @NotBlank(message = "사업자등록번호는 필수입니다.")
        @Pattern(
                regexp = "^\\d{10}$",
                message = "사업자등록번호는 하이픈 없이 숫자 10자리여야 합니다."
        )
        String businessNumber,

        // 매장 상태
        @Size(max = 20, message = "가게 상태는 20글자 최대 20글자 입니다. ")
        String businessStatus,


        @NotNull(message = "주방 처리량은 필수입니다.")
        @Min(value = 1, message = "주방 처리량은 1 이상이어야 합니다.")
        Integer kitchenCapacity,

        @NotNull(message = "영업 시작 시간은 필수입니다.")
        LocalTime openTime,

        @NotNull(message = "영업 종료 시간은 필수입니다.")
        LocalTime closeTime


) {
}
