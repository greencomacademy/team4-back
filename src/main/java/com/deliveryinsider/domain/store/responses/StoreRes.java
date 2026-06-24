package com.deliveryinsider.domain.store.responses;

import com.deliveryinsider.domain.store.enums.BusinessStatus;
import com.deliveryinsider.domain.store.enums.OperationStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record StoreRes(

        Long id,
        
        String phone,

        String storeName,

        String phone,

        String businessNumber,
        
        // 사업자번호 유효 상태
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
        
        // 운영 상태
        OperationStatus operationStatus,

        LocalDateTime createdAt,

        LocalDateTime updatedAt


) {

}
