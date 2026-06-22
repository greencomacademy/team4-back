package com.deliveryinsider.domain.order.entities;

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
public class OrderItem {

    // order_items.id
    private Long id;

    // 이 상세 항목이 소속된 orders.id
    private Long orderId;

    // 주문에 사용된 원본 menus.id
    private Long menuId;

    // 주문 수량
    private Integer quantity;

    /*
     * 아래 값들은 주문 당시 메뉴 정보의 스냅샷이다.
     * 나중에 메뉴가 수정되어도 과거 주문 정보는 바뀌지 않는다.
     */

    // 주문 당시 메뉴명
    private String orderedMenuName;

    // 주문 당시 메뉴 1개 판매가
    private Integer orderedMenuPrice;

    // 주문 당시 메뉴 1개 원가
    private Integer orderedMenuCost;

    // 주문 당시 메뉴 1개 포장비
    private Integer orderedPackagingFee;

    // 주문 당시 1회 예상 조리시간
    private Integer orderedCookingTime;

    // 주문 당시 한 번에 조리 가능한 수량
    private Integer orderedBatchCapacity;

    /*
     * 수량을 반영해서 계산한 항목별 합계
     */

    // orderedMenuPrice × quantity
    private Integer itemMenuAmount;

    // orderedMenuCost × quantity
    private Integer itemMenuCost;

    // orderedPackagingFee × quantity
    private Integer itemPackagingAmount;

    /*
     * 올림(quantity ÷ orderedBatchCapacity)
     * × orderedCookingTime
     */
    private Integer itemCookingTime;

    private LocalDateTime createdAt;
}
