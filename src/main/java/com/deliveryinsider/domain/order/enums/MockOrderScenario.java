package com.deliveryinsider.domain.order.enums;

public enum MockOrderScenario {

    // 일반적인 메뉴와 수량 위주
    NORMAL,

    // 요구사항에 분쟁 가능 / 과도 요청이 포함된 주문
    REQUEST_RISK,

    // 알러지 관련 요구사항이 포함된 주문
    ALLERGY,

    /*
     * 발표 시연용 지연 테스트.
     * 생성 상태는 WAITING으로 두고,
     * 점주가 COOKING으로 변경한 뒤 지연 위험 계산을 확인한다.
     */
    DELAY_TEST,

    // 예상 순수익이 0 이하가 되도록 만든 손실 위험 주문
    LOSS,

    /*
     * 발표용 피크타임 세트.
     * NORMAL / REQUEST_RISK / ALLERGY / DELAY_TEST / LOSS를
     * 각각 1건씩 생성한다.
     */
    PEAK_SET,

    // 여러 시나리오를 섞어서 생성
    MIXED,

    // 메뉴 총수량이 많은 단체주문 위주
    GROUP,

    // 고가 또는 조리시간이 긴 메뉴 위주
    PREMIUM
}
