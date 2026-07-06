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
public class OrderRequest {

    // order_requests.id
    private Long id;

    // 연결 주문 번호
    private Long orderId;

    // 고객 요구사항 원문
    private String requestText;

    // NONE / DELIVERY / ALLERGY / DISPUTE / EXCESSIVE 등
    private String riskType;

    // NORMAL / CAUTION / WARNING / DANGER 등
    private String riskLevel;

    // 감지된 키워드 목록
    private String detectedKeywords;

    // 점주에게 보여줄 안내 메시지
    private String analysisMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}