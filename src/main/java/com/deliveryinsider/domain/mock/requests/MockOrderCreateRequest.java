package com.deliveryinsider.domain.mock.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MockOrderCreateRequest {
    private Integer count;

    private String scenario;
}
