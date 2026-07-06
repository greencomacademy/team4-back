package com.deliveryinsider.domain.order.enums;

public enum OrderCancelType {

    // 고객이 취소를 요청한 경우
    CUSTOMER_REQUEST,

    // 재료 소진
    OUT_OF_STOCK,

    // 조리 지연으로 취소
    COOKING_DELAY,

    // 고객 요구사항을 처리할 수 없는 경우
    REQUEST_UNAVAILABLE,

    // 배달 관련 문제
    DELIVERY_ISSUE,

    // 기타
    ETC
}