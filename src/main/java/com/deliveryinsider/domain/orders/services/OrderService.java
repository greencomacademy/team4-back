package com.deliveryinsider.domain.orders.services;

import com.deliveryinsider.domain.orders.mapper.OrderItemMapper;
import com.deliveryinsider.domain.orders.mapper.OrderMapper;
import com.deliveryinsider.domain.orders.projections.OrderListProjection;
import com.deliveryinsider.domain.orders.responses.OrderListResponse;
import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreMapper storeMapper;
    
    public List<OrderListResponse> findAllOrders( 
        Long userId,
        PlatformType platformType,
        OrderStatus orderStatus){
        // 1. 로그인 사용자의 활성 매장 확인
        Store store = getActiveStore(userId);

        // 2. 플랫폼 및 주문 상태 조건으로 주문 목록 조회
        List<OrderListProjection> orders =
            orderMapper.findAllByStoreId(
                store.getId(),
                platformType,
                orderStatus
            );

        // 3. 조회 결과를 프론트 응답 DTO로 변환
        return orders.stream()
            .map(this::toOrderListResponse)
            .toList();
    }
}
