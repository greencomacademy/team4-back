package com.deliveryinsider.domain.order.mapper;

import com.deliveryinsider.domain.order.entities.OrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderRefundMapper {

    int save(OrderRefund orderRefund);

    OrderRefund findByOrderId(
        @Param("orderId") Long orderId
    );
}