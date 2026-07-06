package com.deliveryinsider.domain.mock.requests;

import com.deliveryinsider.domain.order.enums.MockOrderScenario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

public record MockOrderCreateRequest (
        @Min(
                value = 1,
                message = "Mock 주문 생성 수량은 1개 이상이어야 합니다."
        )
        @Max(
                value = 50,
                message = "Mock 주문은 한 번에 최대 50개까지 생성할 수 있습니다."
        )
        Integer count,

        MockOrderScenario scenario

) {

    /**
     * 요청에서 count를 생략했을 때 사용할 기본값
     */
    public int resolvedCount() {
        return count == null ? 5 : count;
    }

    /**
     * 요청에서 scenario를 생략했을 때 사용할 기본값
     */
    public MockOrderScenario resolvedScenario() {
        return scenario == null
                ? MockOrderScenario.MIXED
                : scenario;
    }
}