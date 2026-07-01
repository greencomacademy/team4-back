package com.deliveryinsider.domain.order.projections;

import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderListProjection {

    private Long id;
    private String orderNo;
    private String platformOrderNumber;
    private PlatformType platformType;
    private OrderStatus orderStatus;

    private Integer totalAmount;
    private Integer netProfit;
    private Integer totalCookingTime;

    // order_items의 quantity 합계
    private Integer totalQuantity;

    // 예: 묵은지 김치찜 외 2개
    private String menuSummary;

    private LocalDateTime orderedAt;
    private String deliveryAddress;
    private LocalDateTime cookingStartedAt;
    
    // 고객 요구사항 요약
    private String requestText;
    private String requestRiskType;
    private String requestRiskLevel;
    private String requestAnalysisMessage;

    // 취소 이력 요약
    private String cancelType;
    private String cancelReason;
    private LocalDateTime canceledAt;
}
