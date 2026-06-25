package com.deliveryinsider.domain.report.projections;

import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 운영 리포트 주문 목록 SQL 결과 Projection
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportOrderProjection {

    private Long id;

    private String orderNo;

    private String platformOrderNumber;

    private PlatformType platformType;

    private OrderStatus orderStatus;

    private String menuSummary;

    private Integer totalQuantity;

    private String deliveryAddress;

    private Integer totalAmount;

    private Integer netProfit;

    private LocalDateTime orderedAt;

    private LocalDateTime cookingStartedAt;

    private LocalDateTime canceledAt;

    private String requestText;

    private String requestRiskType;

    private String requestRiskLevel;

    private String requestAnalysisMessage;

    private String cancelType;

    private String cancelReason;
}
