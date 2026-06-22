package com.deliveryinsider.domain.order.services;

import com.deliveryinsider.domain.order.enums.DelayRiskLevel;
import com.deliveryinsider.domain.order.mapper.OrderMapper;
import com.deliveryinsider.domain.order.projections.OrderListProjection;
import com.deliveryinsider.domain.order.responses.CookingDelayResponse;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DelayRiskService {

    private static final BigDecimal WARNING_STANDARD =
        BigDecimal.valueOf(70);

    private static final BigDecimal DELAYED_STANDARD =
        BigDecimal.valueOf(100);

    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;

    /**
     * 로그인 사용자의 활성 매장에서
     * 현재 조리중인 주문들의 지연 위험을 계산한다.
     */
    @Transactional(readOnly = true)
    public List<CookingDelayResponse> findDelayRisks(
        Long userId
    ) {
        /*
         * 1. 로그인 사용자의 활성 매장 조회
         *
         * 지연 위험은 매장의 kitchenCapacity를 사용하므로
         * 주문보다 먼저 매장을 확인해야 한다.
         */
        Store store = getActiveStore(userId);

        /*
         * 2. 주방 처리량 검증
         *
         * 정상적인 매장 등록 요청에서는 1 이상만 저장되지만,
         * DB를 직접 수정한 비정상 데이터까지 방어한다.
         */
        int kitchenCapacity =
            validateKitchenCapacity(
                store.getKitchenCapacity()
            );

        /*
         * 3. 현재 COOKING 상태인 주문만 조회
         *
         * platformType은 null이므로 플랫폼 필터는 적용하지 않고,
         * orderStatus만 COOKING으로 제한한다.
         */
        List<OrderListProjection> cookingOrders =
            orderMapper.findAllByStoreId(
                store.getId(),
                null,
                OrderStatus.COOKING
            );

        /*
         * 4. 현재 조리중 주문 수
         *
         * WAITING, DELIVERING, COMPLETED,
         * CANCELED, REFUNDED는 포함되지 않는다.
         */
        int currentCookingOrderCount =
            cookingOrders.size();

        /*
         * 조리중 주문이 없으면 계산할 대상도 없으므로
         * 빈 배열을 반환한다.
         */
        if (currentCookingOrderCount == 0) {
            return List.of();
        }

        /*
         * 5. 주방 부하 배수 계산
         *
         * ceil(현재 조리중 주문 수 / kitchenCapacity)
         *
         * 예:
         * 5건 / 처리량 3건
         * → ceil(1.666...)
         * → 2배
         */
        int loadMultiplier =
            calculateLoadMultiplier(
                currentCookingOrderCount,
                kitchenCapacity
            );

        /*
         * 모든 주문을 동일한 기준 시각으로 계산하기 위해
         * 현재 시간을 반복문 밖에서 한 번만 구한다.
         */
        LocalDateTime now = LocalDateTime.now();

        /*
         * 6. 각 조리중 주문을 지연 위험 응답으로 변환
         */
        return cookingOrders.stream()
            .map(order -> toCookingDelayRes(
                order,
                currentCookingOrderCount,
                kitchenCapacity,
                loadMultiplier,
                now
            ))
            .toList();
    }

    /**
     * 로그인 사용자의 활성 매장 조회
     */
    private Store getActiveStore(Long userId) {
        Store store =
            storeMapper.findByUserId(userId);

        if (store == null) {
            throw new DeletedRecordException(
                "등록된 활성 매장이 없습니다."
            );
        }

        return store;
    }

    /**
     * kitchenCapacity가 계산 가능한 값인지 검사한다.
     */
    private int validateKitchenCapacity(
        Integer kitchenCapacity
    ) {
        if (
            kitchenCapacity == null
                || kitchenCapacity <= 0
        ) {
            /*
             * 사용자의 잘못된 요청이 아니라
             * 서버 또는 DB의 비정상 데이터이므로
             * BadRequestException을 사용하지 않는다.
             */
            throw new IllegalStateException(
                "매장의 주방 처리량이 올바르지 않습니다."
            );
        }

        return kitchenCapacity;
    }

    /**
     * 주방 부하 배수 계산
     *
     * 정수 나눗셈을 이용한 올림 계산:
     *
     * (현재 주문 수 + 처리량 - 1) / 처리량
     */
    private int calculateLoadMultiplier(
        int currentCookingOrderCount,
        int kitchenCapacity
    ) {
        return (
            currentCookingOrderCount
                + kitchenCapacity
                - 1
        ) / kitchenCapacity;
    }

    /**
     * 주문 한 건을 CookingDelayRes로 변환한다.
     */
    private CookingDelayResponse toCookingDelayRes(
        OrderListProjection order,
        int currentCookingOrderCount,
        int kitchenCapacity,
        int loadMultiplier,
        LocalDateTime now
    ) {
        /*
         * 1. 주문 예상 조리시간 검증
         */
        int totalCookingTime =
            validateTotalCookingTime(order);

        /*
         * 2. 조리 시작 시각 검증
         */
        LocalDateTime cookingStartedAt =
            validateCookingStartedAt(order);

        /*
         * 3. 조리 시작 후 경과한 시간 계산
         */
        long elapsedMinutes =
            calculateElapsedMinutes(
                cookingStartedAt,
                now
            );

        /*
         * 4. 주방 부하를 반영한 조정 예상 조리시간
         */
        int adjustedCookingTime =
            totalCookingTime * loadMultiplier;

        /*
         * 5. 내부 판정용 진행률 계산
         *
         * 소수점 여섯 자리까지 계산한 후
         * 위험 단계를 판단한다.
         */
        BigDecimal rawProgressRate =
            calculateRawProgressRate(
                elapsedMinutes,
                adjustedCookingTime
            );

        /*
         * 6. SAFE / WARNING / DELAYED 판정
         */
        DelayRiskLevel delayRiskLevel =
            determineDelayRiskLevel(
                rawProgressRate
            );

        /*
         * 7. 프론트 표시용 진행률은
         * 소수점 둘째 자리까지 반올림한다.
         */
        BigDecimal progressRate =
            rawProgressRate.setScale(
                2,
                RoundingMode.HALF_UP
            );

        /*
         * 8. 최종 응답 DTO 생성
         */
        return CookingDelayResponse.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .platformType(order.getPlatformType())
            .orderStatus(order.getOrderStatus())
            .totalQuantity(order.getTotalQuantity())
            .menuSummary(order.getMenuSummary())
            .totalCookingTime(totalCookingTime)
            .cookingStartedAt(cookingStartedAt)
            .elapsedMinutes(elapsedMinutes)
            .currentCookingOrderCount(
                currentCookingOrderCount
            )
            .kitchenCapacity(kitchenCapacity)
            .loadMultiplier(loadMultiplier)
            .adjustedCookingTime(
                adjustedCookingTime
            )
            .progressRate(progressRate)
            .delayRiskLevel(delayRiskLevel)
            .build();
    }

    /**
     * 주문 예상 조리시간 검증
     */
    private int validateTotalCookingTime(
        OrderListProjection order
    ) {
        Integer totalCookingTime =
            order.getTotalCookingTime();

        if (
            totalCookingTime == null
                || totalCookingTime <= 0
        ) {
            throw new IllegalStateException(
                "주문의 예상 조리시간이 올바르지 않습니다. orderId="
                    + order.getId()
            );
        }

        return totalCookingTime;
    }

    /**
     * COOKING 주문의 조리 시작 시각 검증
     */
    private LocalDateTime validateCookingStartedAt(
        OrderListProjection order
    ) {
        LocalDateTime cookingStartedAt =
            order.getCookingStartedAt();

        if (cookingStartedAt == null) {
            throw new IllegalStateException(
                "조리중 주문의 조리 시작 시간이 없습니다. orderId="
                    + order.getId()
            );
        }

        return cookingStartedAt;
    }

    /**
     * 조리 시작 후 경과시간 계산
     */
    private long calculateElapsedMinutes(
        LocalDateTime cookingStartedAt,
        LocalDateTime now
    ) {
        long elapsedMinutes =
            Duration.between(
                cookingStartedAt,
                now
            ).toMinutes();

        /*
         * 서버 시간이나 DB 시간 불일치로
         * 조리 시작 시각이 미래로 잡힌 경우
         * 음수를 반환하지 않고 0분으로 처리한다.
         */
        return Math.max(elapsedMinutes, 0);
    }

    /**
     * 진행률 계산
     *
     * 경과시간 / 조정 예상 조리시간 × 100
     */
    private BigDecimal calculateRawProgressRate(
        long elapsedMinutes,
        int adjustedCookingTime
    ) {
        return BigDecimal.valueOf(elapsedMinutes)
            .multiply(BigDecimal.valueOf(100))
            .divide(
                BigDecimal.valueOf(
                    adjustedCookingTime
                ),
                6,
                RoundingMode.HALF_UP
            );
    }

    /**
     * 진행률에 따른 지연 위험 단계 판정
     */
    private DelayRiskLevel determineDelayRiskLevel(
        BigDecimal progressRate
    ) {
        if (
            progressRate.compareTo(
                DELAYED_STANDARD
            ) >= 0
        ) {
            return DelayRiskLevel.DELAYED;
        }

        if (
            progressRate.compareTo(
                WARNING_STANDARD
            ) >= 0
        ) {
            return DelayRiskLevel.WARNING;
        }

        return DelayRiskLevel.SAFE;
    }
}
