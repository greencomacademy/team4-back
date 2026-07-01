package com.deliveryinsider.domain.order.mapper;

import com.deliveryinsider.domain.order.entities.OrderCancellation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderCancellationMapper {

    // 주문 취소 사유 저장
    int save(OrderCancellation orderCancellation);

    // 2-3단계 주문 상세 조회에서 사용할 예정
    OrderCancellation findByOrderId(
        @Param("orderId") Long orderId
    );
}