package com.deliveryinsider.domain.orders.mapper;

import com.deliveryinsider.domain.orders.entities.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    Order findAllByStoreId(Long id, Long storeId);
}
