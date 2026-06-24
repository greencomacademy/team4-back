package com.deliveryinsider.domain.menus.enums;

/**
 * 메뉴 한 건의 조리 부담 단계
 * 예상 조리시간과 한 번에 조리 가능한 수량을 이용해
 * DashboardService에서 조리 부담 점수를 계산한 뒤,
 * 그 결과를 표현하는 enum이다.
 */
public enum CookingBurdenLevel {

    /**
     * 조리 부담이 낮은 메뉴
     * 조리 부담 점수 3 이하
     */
    LOW,

    /**
     * 일반적인 조리 부담
     * 조리 부담 점수 3 초과, 6 이하
     */
    NORMAL,

    /**
     * 피크 시간대에 주의가 필요한 메뉴
     * 조리 부담 점수 6 초과, 12 이하
     */
    HIGH,

    /**
     * 조리시간이 길거나 동시 처리량이 낮아
     * 주방 병목을 만들 가능성이 높은 메뉴
     * 조리 부담 점수 12 초과
     */
    OVERLOAD
}