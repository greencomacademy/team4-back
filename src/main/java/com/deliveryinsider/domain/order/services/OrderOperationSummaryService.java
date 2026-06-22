package com.deliveryinsider.domain.order.services;

import com.deliveryinsider.domain.order.enums.DelayRiskLevel;
import com.deliveryinsider.global.enums.KitchenLoadLevel;
import com.deliveryinsider.domain.order.mapper.OrderMapper;
import com.deliveryinsider.domain.order.projections.OrderOperationSummaryProjection;
import com.deliveryinsider.domain.order.responses.CookingDelayResponse;
import com.deliveryinsider.domain.order.responses.OrderOperationSummaryResponse;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderOperationSummaryService {

    private final StoreMapper storeMapper;
    private final OrderMapper orderMapper;
    private final DelayRiskService delayRiskService;

    /**
     * 로그인 사용자의 현재 주문 운영 상태를 요약한다.
     * 진행 주문 수, 상태별 주문 수, 예상 순수익,
     * 지연 위험 건수와 주방 부하 단계를 한 번에 반환한다.
     */
    @Transactional(readOnly = true)
    public OrderOperationSummaryResponse findOperationSummary(
        Long userId
    ) {
        // 1. 로그인 사용자의 활성 매장 조회
        Store store = getActiveStore(userId);

        // 2. 주방 처리량 검증
        int kitchenCapacity =
            validateKitchenCapacity(
                store.getKitchenCapacity()
            );

        // 3. 진행 주문의 상태별 개수와 예상 순수익 조회
        OrderOperationSummaryProjection projection =
            orderMapper.findOperationSummary(
                store.getId()
            );

        if (projection == null) {
            throw new IllegalStateException(
                "주문 운영 요약 집계 결과가 없습니다."
            );
        }

        /*
         * MyBatis 집계 결과를 null-safe 값으로 변환한다.
         *
         * COUNT와 COALESCE를 사용하므로 정상 SQL 결과는 0이지만,
         * 비정상 매핑이나 테스트 Mock의 null 값도 방어한다.
         */
        long progressOrderCount =
            getSafeLong(
                projection.getProgressOrderCount()
            );

        long waitingCount =
            getSafeLong(
                projection.getWaitingCount()
            );

        long cookingCount =
            getSafeLong(
                projection.getCookingCount()
            );

        long deliveringCount =
            getSafeLong(
                projection.getDeliveringCount()
            );

        long expectedProgressNetProfit =
            getSafeLong(
                projection.getExpectedProgressNetProfit()
            );

        // 4. 현재 COOKING 주문들의 지연 위험 계산 결과 조회
        List<CookingDelayResponse> delayRisks =
            delayRiskService.findDelayRisks(userId);

        // 5. WARNING과 DELAYED 주문만 지연 위험 건수에 포함
        long delayRiskCount =
            delayRisks.stream()
                .filter(delay ->
                    delay.delayRiskLevel()
                        == DelayRiskLevel.WARNING
                        || delay.delayRiskLevel()
                        == DelayRiskLevel.DELAYED
                )
                .count();

        // 6. COOKING 주문 수를 기준으로 현재 주방 부하 단계 계산
        KitchenLoadLevel kitchenLoadLevel =
            determineKitchenLoadLevel(
                cookingCount,
                kitchenCapacity
            );

        // 7. 현재 운영 상태에 맞는 점주 안내 문구 생성
        String message = createMessage(
            kitchenLoadLevel,
            delayRiskCount,
            waitingCount
        );

        // 8. 최종 주문 운영 요약 응답 생성
        return OrderOperationSummaryResponse.builder()
            .progressOrderCount(progressOrderCount)
            .waitingCount(waitingCount)
            .cookingCount(cookingCount)
            .deliveringCount(deliveringCount)
            .delayRiskCount(delayRiskCount)
            .expectedProgressNetProfit(
                expectedProgressNetProfit
            )
            .kitchenCapacity(kitchenCapacity)
            .kitchenLoadLevel(kitchenLoadLevel)
            .message(message)
            .build();
    }

    /**
     * 로그인 사용자의 활성 매장을 조회한다.
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
     * kitchenCapacity가 주방 부하 계산에 사용할 수 있는 값인지 검사한다.
     */
    private int validateKitchenCapacity(
        Integer kitchenCapacity
    ) {
        if (
            kitchenCapacity == null
                || kitchenCapacity <= 0
        ) {
            throw new IllegalStateException(
                "매장의 주방 처리량이 올바르지 않습니다."
            );
        }

        return kitchenCapacity;
    }

    /**
     * 현재 COOKING 주문 수를 기준으로 주방 부하 단계를 계산한다.
     */
    private KitchenLoadLevel determineKitchenLoadLevel(
        long cookingCount,
        int kitchenCapacity
    ) {
        if (cookingCount == 0) {
            return KitchenLoadLevel.LOW;
        }

        if (cookingCount <= kitchenCapacity) {
            return KitchenLoadLevel.NORMAL;
        }

        if (
            cookingCount
                <= (long) kitchenCapacity * 2
        ) {
            return KitchenLoadLevel.HIGH;
        }

        return KitchenLoadLevel.OVERLOAD;
    }

    /**
     * 현재 주방 부하와 지연 위험 건수를 바탕으로
     * 점주에게 보여줄 운영 안내 문구를 만든다.
     */
    private String createMessage(
        KitchenLoadLevel kitchenLoadLevel,
        long delayRiskCount,
        long waitingCount
    ) {
        if (delayRiskCount > 0) {
            return "지연 위험 주문이 "
                + delayRiskCount
                + "건 있습니다. 조리중 주문을 우선 확인해 주세요.";
        }

        return switch (kitchenLoadLevel) {
            case LOW -> {
                if (waitingCount > 0) {
                    yield "현재 조리중 주문은 없습니다. 접수대기 주문을 확인해 주세요.";
                }

                yield "현재 진행할 조리 주문이 없어 주방에 여유가 있습니다.";
            }

            case NORMAL ->
                "현재 주방 처리량 이내에서 정상적으로 운영 중입니다.";

            case HIGH ->
                "주방 처리량을 초과했습니다. 조리 순서와 신규 주문 접수를 확인해 주세요.";

            case OVERLOAD ->
                "주방 과부하 상태입니다. 조리중 주문을 우선 처리해 주세요.";
        };
    }

    /**
     * MyBatis 집계 결과가 null인 경우 0으로 변환한다.
     */
    private long getSafeLong(Long value) {
        return value == null ? 0L : value;
    }
}
