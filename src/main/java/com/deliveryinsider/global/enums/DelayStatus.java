package com.deliveryinsider.global.enums;

/**
 * 조리중 주문의 지연 위험 상태다.
 * 주문 진행 상태인 OrderStatus와는 다른 개념이다.
 * 예:
 * orderStatus = COOKING
 * delayStatus = TIME_OVER
 * 주문은 여전히 조리중이지만
 * 예상 조리시간은 초과했다는 의미다.
 */
public enum DelayStatus {

    /**
     * 시간과 주방 대기열 모두 정상
     */
    NORMAL,

    /**
     * 예상 조리시간을 초과한 상태
     */
    TIME_OVER,

    /**
     * 현재 조리중 주문 수가 주방 처리량을 초과한 상태
     */
    QUEUE_DELAY,

    /**
     * 시간 초과와 대기열 초과가 동시에 발생한 상태
     */
    TIME_AND_QUEUE_DELAY
}
