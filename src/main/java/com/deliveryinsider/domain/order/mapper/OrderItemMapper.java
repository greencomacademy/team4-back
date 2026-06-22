package com.deliveryinsider.domain.order.mapper;

import com.deliveryinsider.domain.order.entities.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    // 한 주문의 상세 메뉴들을 일괄 저장
    int saveAll(
        @Param("items") List<OrderItem> items
    );

    // 주문에 포함된 상세 메뉴 전체 조회
    List<OrderItem> findAllByOrderId(
        @Param("orderId") Long orderId
    );
}
