package com.deliveryinsider.domain.store.entities;

import com.deliveryinsider.domain.store.enums.BusinessStatus;
import com.deliveryinsider.domain.store.enums.OperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    private Long id;

    // 매장을 소유한 회원 PK
    private Long userId;

    private String storeName;
    private String phone;

    private String address;
    private String addressDetail;

    private String industryType;
    private String businessNumber;
    private BusinessStatus businessStatus;

    private LocalDateTime businessVerifiedAt;
    
    private OperationStatus operationStatus;




    // 동시에 처리 가능한 주문 수
    private Integer kitchenCapacity;

    // 매일 반복되는 영업 시작 시각
    private LocalTime openTime;

    // 매일 반복되는 영업 종료 시각
    private LocalTime closeTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}