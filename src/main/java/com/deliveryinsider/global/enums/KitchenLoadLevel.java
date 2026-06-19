package com.deliveryinsider.global.enums;

/**
 * 현재 매장 전체의 주방 부하 수준이다.
 * 개별 주문의 지연 상태가 아니라
 * 매장 전체의 COOKING 주문 수와
 * kitchenCapacity를 비교해서 계산한다.
 */
public enum KitchenLoadLevel {

    /**
     * 주방에 충분한 여유가 있는 상태
     */
    LOW,

    /**
     * 설정한 주방 처리량 이내인 상태
     */
    NORMAL,

    /**
     * 주방 처리량을 초과했지만
     * 2배 미만인 상태
     */
    HIGH,

    /**
     * 주방 처리량의 2배 이상인 상태
     */
    OVERLOAD
}