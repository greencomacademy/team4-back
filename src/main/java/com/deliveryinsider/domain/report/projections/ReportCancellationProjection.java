package com.deliveryinsider.domain.report.projections;

import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 취소 리포트 목록 SQL 결과 Projection
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCancellationProjection {

    private Long id;

    private String orderNo;

    private String platformOrderNumber;

    private PlatformType platformType;

    private OrderStatus previousStatus;

    private String menuSummary;

    private Integer totalQuantity;

    private Integer totalAmount;

    private Integer netProfit;

    private String cancelType;

    private String cancelReason;

    private String canceledByType;

    private Long canceledByUserId;

    private LocalDateTime orderedAt;

    private LocalDateTime canceledAt;

    private String requestText;

    private String requestRiskType;

    private String requestRiskLevel;
}
