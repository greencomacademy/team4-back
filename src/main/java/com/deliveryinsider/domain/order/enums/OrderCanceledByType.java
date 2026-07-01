package com.deliveryinsider.domain.order.enums;

public enum OrderCanceledByType {

    // 점주가 직접 취소
    OWNER,

    // 고객 취소
    CUSTOMER,

    // 플랫폼 취소
    PLATFORM,

    // 시스템 자동 취소
    SYSTEM
}