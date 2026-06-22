package com.deliveryinsider.domain.orders.responses;

import lombok.Builder;

@Builder
public record OrderItemResponse(

    Long id,
    Long menuId,
    Integer quantity,

    String orderedMenuName,
    Integer orderedMenuPrice,
    Integer orderedMenuCost,
    Integer orderedPackagingFee,
    Integer orderedCookingTime,
    Integer orderedBatchCapacity,

    Integer itemMenuAmount,
    Integer itemMenuCost,
    Integer itemPackagingAmount,
    Integer itemCookingTime

) {
}
