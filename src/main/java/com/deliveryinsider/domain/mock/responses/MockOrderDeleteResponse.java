package com.deliveryinsider.domain.mock.responses;

import lombok.Builder;

@Builder
public record MockOrderDeleteResponse (

    Integer deletedOrderCount
) {

}
