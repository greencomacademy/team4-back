package com.deliveryinsider.domain.mock.services;

import com.deliveryinsider.domain.mock.mapper.MockOrderMapper;
import com.deliveryinsider.domain.mock.requests.MockOrderCreateRequest;
import com.deliveryinsider.domain.mock.responses.MockOrderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MockOrderService {

    public MockOrderCreateResponse createMockOrders(
            Long userId,
            MockOrderCreateRequest request) {

        // TODO 1. 요청값 정리
        // TODO 2. Store 조회
        // TODO 3. Menu 조회
        // TODO 4. PlatformSetting 조회
        // TODO 5. 시나리오 선택
        // TODO 6. orders 저장
        // TODO 7. order_items 저장

        return new MockOrderCreateResponse(0);
    }

    public void deleteMockOrders(Long userId) {
        // TODO
    }

}
