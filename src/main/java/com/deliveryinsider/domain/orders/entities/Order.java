package com.deliveryinsider.domain.orders.entities;

import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.enums.OrderStatus;
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
public class Order {

    // orders.id
    private Long id;

    // 주문이 들어온 매장 PK
    private Long storeId;

    // 화면에 표시할 주문번호
    private String orderNo;

    // 배민, 쿠팡이츠, 요기요, 땡겨요
    private PlatformType platformType;

    // 현재 주문 상태
    private OrderStatus orderStatus;

    // 주문 메뉴 판매가 합계
    private Integer totalAmount;

    // 주문 생성 당시 계산한 플랫폼 수수료
    private Integer commissionAmount;

    // 주문 생성 당시 점주 부담 쿠폰 금액
    private Integer couponCost;

    // 주문 생성 당시 점주 부담 배달비
    private Integer deliveryFee;

    // 주문 생성 당시 플랫폼 지원금
    private Integer platformSupportAmount;

    // 주문 메뉴 원가 총합
    private Integer totalMenuCost;

    // 주문 메뉴 포장비 총합
    private Integer totalPackagingFee;

    // 최종 예상 순수익
    private Integer netProfit;

    // 주문 전체 예상 조리시간
    private Integer totalCookingTime;

    // 실제 주문이 들어온 시각
    private LocalDateTime orderedAt;

    // WAITING에서 COOKING으로 변경된 시각
    private LocalDateTime cookingStartedAt;

    // 완료된 시각
    private LocalDateTime completedAt;

    // 취소된 시각
    private LocalDateTime canceledAt;

    // 환불된 시각
    private LocalDateTime refundedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
