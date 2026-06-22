package com.deliveryinsider.domain.order.mapper;

import com.deliveryinsider.domain.order.projections.OrderOperationSummaryProjection;
import com.deliveryinsider.domain.order.entities.Order;
import com.deliveryinsider.domain.order.projections.OrderListProjection;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.enums.OrderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    // 주문 전체 정보 저장
    int save(Order order);

    // 로그인 사용자의 매장 소유권까지 검사하며 주문 한 건 조회
    Order findByIdAndStoreId(
        @Param("id") Long id,
        @Param("storeId") Long storeId
    );
    
    List<OrderListProjection> findAllByStoreId(
        @Param("storeId") Long storeId,
        @Param("platformType") PlatformType platformType,
        @Param("orderStatus") OrderStatus orderStatus
    );
    int updateStatus(
        @Param("id") Long id,
        @Param("storeId") Long storeId,
        @Param("currentStatus") OrderStatus currentStatus,
        @Param("nextStatus") OrderStatus nextStatus
    );
    // 현재 진행 중인 주문의 상태별 개수와 예상 순수익 집계
    OrderOperationSummaryProjection findOperationSummary(
        @Param("storeId") Long storeId
    );
    // 로그인 사용자의 매장에 속한 주문 전체 삭제 - 현재 1차는 mock주문만 넣고 삭제함 
    int deleteAllByStoreId(
        @Param("storeId") Long storeId
    );
}
