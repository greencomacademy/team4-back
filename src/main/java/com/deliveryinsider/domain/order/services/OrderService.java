package com.deliveryinsider.domain.order.services;

import com.deliveryinsider.domain.order.entities.Order;
import com.deliveryinsider.domain.order.entities.OrderItem;
import com.deliveryinsider.domain.order.mapper.OrderItemMapper;
import com.deliveryinsider.domain.order.mapper.OrderMapper;
import com.deliveryinsider.domain.order.projections.OrderListProjection;
import com.deliveryinsider.domain.order.requests.OrderStatusUpdateRequest;
import com.deliveryinsider.domain.order.responses.OrderDetailResponse;
import com.deliveryinsider.domain.order.responses.OrderItemResponse;
import com.deliveryinsider.domain.order.responses.OrderListResponse;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.enums.OrderStatus;

import com.deliveryinsider.global.errors.custom.BadRequestException;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreMapper storeMapper;

    /**
     * 내 활성 매장의 주문 목록 조회
     * platformType과 orderStatus는 null일 수 있으며,
     * null이면 해당 필터를 적용하지 않는다.
     */
    @Transactional(readOnly = true)
    public List<OrderListResponse> findAll(
        Long userId,
        PlatformType platformType,
        OrderStatus orderStatus
    ) {
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
            .map(this::toOrderListRes)
            .toList();
    }

    /**
     * 내 활성 매장의 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse findOne(
        Long userId,
        Long orderId
    ) {
        // 1. 로그인 사용자의 활성 매장 확인
        Store store = getActiveStore(userId);

        /*
         * 2. orderId와 storeId를 함께 사용해 주문 조회
         *
         * 다른 사용자가 타 매장의 주문 ID를 알아도
         * 조회하지 못하도록 소유권을 검사한다.
         */
        Order order = orderMapper.findByIdAndStoreId(
            orderId,
            store.getId()
        );

        if (order == null) {
            throw new DeletedRecordException(
                "주문을 찾을 수 없습니다."
            );
        }

        // 3. 해당 주문에 포함된 메뉴 상세 조회
        List<OrderItem> orderItems =
            orderItemMapper.findAllByOrderId(orderId);

        // 4. 주문 전체 정보와 상세 메뉴를 묶어서 응답
        return toOrderDetailRes(order, orderItems);
    }

    /**
     * 로그인 사용자의 활성 매장 조회
     */
    private Store getActiveStore(Long userId) {
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new DeletedRecordException(
                "등록된 활성 매장이 없습니다."
            );
        }

        return store;
    }

    /**
     * 목록 조회 SQL 결과를 목록 응답 DTO로 변환
     */
    private OrderListResponse toOrderListRes(
        OrderListProjection order
    ) {
        return OrderListResponse.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .platformType(order.getPlatformType())
            .orderStatus(order.getOrderStatus())
            .totalAmount(order.getTotalAmount())
            .netProfit(order.getNetProfit())
            .totalCookingTime(
                order.getTotalCookingTime()
            )
            .totalQuantity(order.getTotalQuantity())
            .menuSummary(order.getMenuSummary())
            .orderedAt(order.getOrderedAt())
            .cookingStartedAt(
                order.getCookingStartedAt()
            )
            .build();
    }

    /**
     * 주문 상세 메뉴 Entity를 응답 DTO로 변환
     */
    private OrderItemResponse toOrderItemRes(
        OrderItem item
    ) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .menuId(item.getMenuId())
            .quantity(item.getQuantity())
            .orderedMenuName(
                item.getOrderedMenuName()
            )
            .orderedMenuPrice(
                item.getOrderedMenuPrice()
            )
            .orderedMenuCost(
                item.getOrderedMenuCost()
            )
            .orderedPackagingFee(
                item.getOrderedPackagingFee()
            )
            .orderedCookingTime(
                item.getOrderedCookingTime()
            )
            .orderedBatchCapacity(
                item.getOrderedBatchCapacity()
            )
            .itemMenuAmount(
                item.getItemMenuAmount()
            )
            .itemMenuCost(
                item.getItemMenuCost()
            )
            .itemPackagingAmount(
                item.getItemPackagingAmount()
            )
            .itemCookingTime(
                item.getItemCookingTime()
            )
            .build();
    }

    /**
     * 주문 Entity와 주문 상세 목록을 상세 응답 DTO로 변환
     */
    private OrderDetailResponse toOrderDetailRes(
        Order order,
        List<OrderItem> orderItems
    ) {
        List<OrderItemResponse> itemResponses =
            orderItems.stream()
                .map(this::toOrderItemRes)
                .toList();

        return OrderDetailResponse.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .platformType(order.getPlatformType())
            .orderStatus(order.getOrderStatus())

            .totalAmount(order.getTotalAmount())
            .commissionAmount(
                order.getCommissionAmount()
            )
            .couponCost(order.getCouponCost())
            .deliveryFee(order.getDeliveryFee())
            .platformSupportAmount(
                order.getPlatformSupportAmount()
            )
            .totalMenuCost(
                order.getTotalMenuCost()
            )
            .totalPackagingFee(
                order.getTotalPackagingFee()
            )
            .netProfit(order.getNetProfit())

            .totalCookingTime(
                order.getTotalCookingTime()
            )

            .orderedAt(order.getOrderedAt())
            .cookingStartedAt(
                order.getCookingStartedAt()
            )
            .completedAt(order.getCompletedAt())
            .canceledAt(order.getCanceledAt())
            .refundedAt(order.getRefundedAt())

            .items(itemResponses)

            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
    /**
     * 주문 상태 변경
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse updateStatus(
        Long userId,
        Long orderId,
        OrderStatusUpdateRequest updateReq
    ) {
        // 1. 로그인 사용자의 활성 매장 조회
        Store store = getActiveStore(userId);

        // 2. 해당 매장의 주문인지 확인
        Order currentOrder =
            orderMapper.findByIdAndStoreId(
                orderId,
                store.getId()
            );

        if (currentOrder == null) {
            throw new DeletedRecordException(
                "상태를 변경할 주문을 찾을 수 없습니다."
            );
        }

        OrderStatus currentStatus =
            currentOrder.getOrderStatus();

        OrderStatus nextStatus =
            updateReq.orderStatus();

        // 3. 허용된 상태 전환인지 검사
        if (!isAllowedTransition(currentStatus, nextStatus)) {
            throw new BadRequestException(
                String.format(
                    "%s 상태에서는 %s 상태로 변경할 수 없습니다.",
                    currentStatus,
                    nextStatus
                )
            );
        }

        // 4. 상태 변경
        int result = orderMapper.updateStatus(
            orderId,
            store.getId(),
            currentStatus,
            nextStatus
        );

        /*
         * 조회 이후 다른 요청이 먼저 상태를 바꿨다면
         * WHERE order_status = currentStatus 조건이 맞지 않아
         * 결과가 0이 된다.
         */
        if (result != 1) {
            throw new BadRequestException(
                "주문 상태가 이미 변경되었습니다. 새로고침 후 다시 시도해 주세요."
            );
        }

        // 5. 변경된 주문 재조회
        Order updatedOrder =
            orderMapper.findByIdAndStoreId(
                orderId,
                store.getId()
            );

        if (updatedOrder == null) {
            throw new RuntimeException(
                "변경된 주문 정보를 조회할 수 없습니다."
            );
        }

        // 6. 주문 상세 항목 조회
        List<OrderItem> orderItems =
            orderItemMapper.findAllByOrderId(orderId);

        // 7. 변경된 주문 상세 반환
        return toOrderDetailRes(
            updatedOrder,
            orderItems
        );
    }
    /**
     * 현재 상태에서 요청 상태로 변경 가능한지 검사
     */
    private boolean isAllowedTransition(
        OrderStatus currentStatus,
        OrderStatus nextStatus
    ) {
        return switch (currentStatus) {
            case WAITING ->
                nextStatus == OrderStatus.COOKING
                    || nextStatus == OrderStatus.CANCELED;

            case COOKING ->
                nextStatus == OrderStatus.DELIVERING
                    || nextStatus == OrderStatus.CANCELED;

            case DELIVERING ->
                nextStatus == OrderStatus.COMPLETED;

            case COMPLETED ->
                nextStatus == OrderStatus.REFUNDED;

            case CANCELED, REFUNDED ->
                false;
        };
    }
    
    
}
