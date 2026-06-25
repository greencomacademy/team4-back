package com.deliveryinsider.domain.order.mapper;

import com.deliveryinsider.domain.order.entities.OrderRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderRequestMapper {

    OrderRequest findByOrderId(
        @Param("orderId") Long orderId
    );

    int save(OrderRequest orderRequest);
}