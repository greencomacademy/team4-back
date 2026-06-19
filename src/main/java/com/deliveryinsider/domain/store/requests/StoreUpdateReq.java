package com.deliveryinsider.domain.store.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record StoreUpdateReq(

        @Size(max = 100, message = "매장명은 100자 이하여야 합니다.")
        @Pattern(
                regexp = "(?s).*\\S.*",
                message = "매장명은 공백일 수 없습니다."
        )
        String storeName,

        @Pattern(
                regexp = "^\\d{10}$",
                message = "사업자등록번호는 하이픈 없이 숫자 10자리여야 합니다."
        )
        String businessNumber,

        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        @Pattern(
                regexp = "(?s).*\\S.*",
                message = "주소는 공백일 수 없습니다."
        )
        String address,

        /*
         * 빈 문자열을 보내면 상세 주소를 제거할 수 있도록
         * @NotBlank나 공백 검사 패턴을 사용하지 않는다.
         */
        @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
        String addressDetail,

        @Size(max = 50, message = "업종은 50자 이하여야 합니다.")
        @Pattern(
                regexp = "(?s).*\\S.*",
                message = "업종은 공백일 수 없습니다."
        )
        String industryType,

        @Min(value = 1, message = "주방 처리량은 1 이상이어야 합니다.")
        Integer kitchenCapacity,

        /*
         * PATCH 요청이므로 null이면 기존 영업 시작 시간을 유지한다.
         */
        LocalTime openTime,

        /*
         * PATCH 요청이므로 null이면 기존 영업 종료 시간을 유지한다.
         */
        LocalTime closeTime

) {

    /*
     * 모든 필드가 null이면 수정할 내용이 없는 요청이므로 거부한다.
     */
    @AssertTrue(message = "수정할 매장 정보를 하나 이상 입력해야 합니다.")
    public boolean isUpdateFieldPresent() {
        return storeName != null
                || businessNumber != null
                || address != null
                || addressDetail != null
                || industryType != null
                || kitchenCapacity != null
                || openTime != null
                || closeTime != null;
    }
}