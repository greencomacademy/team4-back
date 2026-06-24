package com.deliveryinsider.domain.mock.responses;

import com.deliveryinsider.domain.order.enums.MockOrderScenario;
import lombok.Builder;

import java.util.List;

@Builder
public record MockOrderCreateResponse(

    // 요청받은 생성 개수
    Integer requestedCount,

    // 실제로 생성된 주문 개수
    Integer createdCount,

    // 적용된 Mock 시나리오
    MockOrderScenario scenario,

    // 생성된 orders.id 목록
    List<Long> orderIds,

    // 화면에 표시할 주문번호 목록
    List<String> orderNos
) {

}
