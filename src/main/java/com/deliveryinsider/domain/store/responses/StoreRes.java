package com.deliveryinsider.domain.store.responses;

import com.deliveryinsider.domain.store.enums.BusinessStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record StoreRes(

        Long id,

        String storeName,

        String phone,

        String businessNumber,

        BusinessStatus businessStatus,

        LocalDateTime businessVerifiedAt,

        String address,

        String addressDetail,

        String industryType,

        Integer kitchenCapacity,

        // 매일 반복되는 영업 시작 시각
        LocalTime openTime,

        // 매일 반복되는 영업 종료 시각
        LocalTime closeTime,

        LocalDateTime createdAt,

        LocalDateTime updatedAt


) {

}
