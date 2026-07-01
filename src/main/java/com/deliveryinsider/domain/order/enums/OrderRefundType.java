package com.deliveryinsider.domain.order.enums;

public enum OrderRefundType {

    // 고객이 환불을 요청한 경우
    CUSTOMER_REQUEST,

    // 음식 품질, 누락, 오조리 등 음식 문제
    FOOD_ISSUE,

    // 배달 지연, 오배송, 배달 중 파손 등 배달 문제
    DELIVERY_ISSUE,

    // 매장 조리 실수, 포장 실수 등
    STORE_MISTAKE,

    // 플랫폼 정책에 따른 환불
    PLATFORM_POLICY,

    // 기타
    ETC
}