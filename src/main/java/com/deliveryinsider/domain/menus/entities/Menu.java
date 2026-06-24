package com.deliveryinsider.domain.menus.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    // menus.id
    private Long id;

    // 이 메뉴가 소속된 stores.id
    private Long storeId;

    // 메뉴명
    private String menuName;

    // 고객에게 판매하는 가격
    private Integer menuPrice;

    // 메뉴 1개당 재료 원가
    private Integer menuCost;

    // 메뉴 1개당 포장비
    private Integer packagingFee;

    // 한 번 조리하는 데 걸리는 예상 시간(분)
    private Integer expectedCookingTime;

    // 한 번에 동시에 조리 가능한 수량
    private Integer batchCapacity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}