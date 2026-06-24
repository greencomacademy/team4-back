package com.deliveryinsider.domain.order.enums;

public enum MockOrderScenario {
    // 일반·단체·프리미엄 주문을 적절히 섞어서 생성
    MIXED,

    // 일반적인 메뉴와 수량 위주
    NORMAL,

    // 메뉴 총수량이 많은 단체주문 위주
    GROUP,

    // 고가 또는 조리시간이 긴 메뉴 위주
    PREMIUM,

    // 알러지
    ALLERGY,

    // 손실
    LOSS,

    /*
     * 발표 시연용 지연 테스트.
     * 일반 Mock과 달리 COOKING 상태와 과거 조리 시작 시간을
     * 포함한 주문을 생성할 때 사용한다.
     */
    DELAY_TEST
}
