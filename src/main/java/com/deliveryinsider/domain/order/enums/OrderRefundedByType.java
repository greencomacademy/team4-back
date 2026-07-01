package com.deliveryinsider.domain.order.enums;

public enum OrderRefundedByType {

    // 점주가 직접 환불 처리
    OWNER,

    // 고객 요청 환불
    CUSTOMER,

    // 플랫폼 처리 환불
    PLATFORM,

    // 시스템 자동 환불
    SYSTEM
}