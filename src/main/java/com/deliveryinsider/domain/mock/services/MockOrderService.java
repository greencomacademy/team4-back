package com.deliveryinsider.domain.mock.services;

import com.deliveryinsider.domain.mock.requests.MockOrderCreateRequest;
import com.deliveryinsider.domain.mock.responses.MockOrderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MockOrderService {
    @Transactional
    public MockOrderCreateResponse createMockOrders(
            Long userId,
            MockOrderCreateRequest request
    ) {

        // TODO 1. 요청값(count, scenario) 정리
        // TODO 2. 로그인 사용자의 Store 조회
        // TODO 3. Menu 조회
        // TODO 4. PlatformSetting 조회
        // TODO 5. 시나리오별 Mock 주문 생성
        // TODO 6. orders 저장
        // TODO 7. order_items 저장

        return new MockOrderCreateResponse(0);
    }

    @Transactional
    public void createBasicData(Long userId) {

        // TODO 1. 로그인 사용자의 Store 조회
        // TODO 2. 기본 Menu 생성
        // TODO 3. 기본 PlatformSetting 생성
        // TODO 4. 필요한 샘플 데이터 생성
    }

    @Transactional
    public void deleteMockOrders(Long userId) {

        // TODO 1. 로그인 사용자의 Store 조회
        // TODO 2. 해당 Store의 Mock 주문 삭제
    }

    /**
     * MIXED 시나리오 Mock 주문 생성
     * TODO 구현
     */
    private void createMixedOrders() {

    }

    /**
     * NORMAL 시나리오 Mock 주문 생성
     * TODO 구현
     */
    private void createNormalOrders() {

    }

    /**
     * GROUP 시나리오 Mock 주문 생성
     * TODO 구현
     */
    private void createGroupOrders() {

    }

    /**
     * PREMIUM 시나리오 Mock 주문 생성
     * TODO 구현
     */
    private void createPremiumOrders() {

    }

    /**
     * DELAY_TEST 시나리오 Mock 주문 생성
     * TODO 구현
     */
    private void createDelayTestOrders() {

    }
}

