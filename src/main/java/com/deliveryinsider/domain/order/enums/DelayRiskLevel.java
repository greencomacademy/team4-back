package com.deliveryinsider.domain.order.enums;

/**
 * 조리중 주문의 지연 위험 단계
 * 실제 위험 단계 판단은 DelayRiskService에서 수행하고,
 * 이 enum은 판단 결과를 표현하는 값으로 사용한다.
 */
public enum DelayRiskLevel {

    // 조리 진행률 70% 미만
    SAFE,

    // 조리 진행률 70% 이상, 100% 미만
    WARNING,

    // 조리 진행률 100% 이상
    DELAYED
}
