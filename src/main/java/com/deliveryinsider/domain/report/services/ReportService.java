package com.deliveryinsider.domain.report.services;

import com.deliveryinsider.domain.report.mapper.ReportMapper;
import com.deliveryinsider.domain.report.projections.ReportSummaryProjection;
import com.deliveryinsider.domain.report.requests.ReportSearchRequest;
import com.deliveryinsider.domain.report.responses.ReportSummaryResponse;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.errors.custom.BadRequestException;
import com.deliveryinsider.global.errors.custom.NotRegisteredStoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliveryinsider.domain.report.projections.ReportOrderProjection;
import com.deliveryinsider.domain.report.responses.ReportOrderResponse;
import com.deliveryinsider.domain.report.projections.ReportPlatformProjection;
import com.deliveryinsider.domain.report.responses.ReportPlatformResponse;
import com.deliveryinsider.domain.report.projections.ReportCancellationProjection;
import com.deliveryinsider.domain.report.projections.ReportCancellationSummaryProjection;
import com.deliveryinsider.domain.report.responses.ReportCancellationResponse;
import com.deliveryinsider.domain.report.responses.ReportCancellationSummaryResponse;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ReportService {

    private final StoreMapper storeMapper;
    private final ReportMapper reportMapper;

    /**
     * 운영 리포트 요약 조회
     */
    @Transactional(readOnly = true)
    public ReportSummaryResponse findSummary(
        Long userId,
        ReportSearchRequest searchReq
    ) {
        validateDateRange(searchReq);

        Store store = getActiveStore(userId);

        ReportSummaryProjection projection =
            reportMapper.findSummary(
                store.getId(),
                searchReq
            );

        long totalOrderCount =
            getSafeLong(
                projection.getTotalOrderCount()
            );

        long completedOrderCount =
            getSafeLong(
                projection.getCompletedOrderCount()
            );

        long canceledOrderCount =
            getSafeLong(
                projection.getCanceledOrderCount()
            );

        long totalSales =
            getSafeLong(
                projection.getTotalSales()
            );

        long totalNetProfit =
            getSafeLong(
                projection.getTotalNetProfit()
            );

        long requestRiskCount =
            getSafeLong(
                projection.getRequestRiskCount()
            );

        long lossRiskCount =
            getSafeLong(
                projection.getLossRiskCount()
            );

        double cancelRate =
            calculateCancelRate(
                totalOrderCount,
                canceledOrderCount
            );

        long averageOrderAmount =
            calculateAverageOrderAmount(
                completedOrderCount,
                totalSales
            );

        return ReportSummaryResponse.builder()
            .totalOrderCount(totalOrderCount)
            .completedOrderCount(completedOrderCount)
            .canceledOrderCount(canceledOrderCount)
            .totalSales(totalSales)
            .totalNetProfit(totalNetProfit)
            .cancelRate(cancelRate)
            .averageOrderAmount(averageOrderAmount)
            .requestRiskCount(requestRiskCount)
            .lossRiskCount(lossRiskCount)
            .build();
    }
    /**
     * 운영 리포트 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReportOrderResponse> findOrders(
        Long userId,
        ReportSearchRequest searchReq
    ) {
        validateDateRange(searchReq);

        Store store = getActiveStore(userId);

        List<ReportOrderProjection> orders =
            reportMapper.findOrders(
                store.getId(),
                searchReq
            );

        return orders.stream()
            .map(this::toReportOrderResponse)
            .toList();
    }
    private ReportOrderResponse toReportOrderResponse(
        ReportOrderProjection order
    ) {
        return ReportOrderResponse.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .platformOrderNumber(order.getPlatformOrderNumber())
            .platformType(order.getPlatformType())
            .orderStatus(order.getOrderStatus())
            .menuSummary(order.getMenuSummary())
            .totalQuantity(order.getTotalQuantity())
            .deliveryAddress(order.getDeliveryAddress())
            .totalAmount(order.getTotalAmount())
            .netProfit(order.getNetProfit())
            .orderedAt(order.getOrderedAt())
            .cookingStartedAt(order.getCookingStartedAt())
            .canceledAt(order.getCanceledAt())
            .requestText(order.getRequestText())
            .requestRiskType(order.getRequestRiskType())
            .requestRiskLevel(order.getRequestRiskLevel())
            .requestAnalysisMessage(
                order.getRequestAnalysisMessage()
            )
            .cancelType(order.getCancelType())
            .cancelReason(order.getCancelReason())
            .build();
    }
    /**
     * 플랫폼별 리포트 조회
     */
    @Transactional(readOnly = true)
    public List<ReportPlatformResponse> findPlatforms(
        Long userId,
        ReportSearchRequest searchReq
    ) {
        validateDateRange(searchReq);

        Store store = getActiveStore(userId);

        List<ReportPlatformProjection> platforms =
            reportMapper.findPlatforms(
                store.getId(),
                searchReq
            );

        return platforms.stream()
            .map(this::toReportPlatformResponse)
            .toList();
    }
    /**
     * 취소 리포트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReportCancellationResponse> findCancellations(
        Long userId,
        ReportSearchRequest searchReq
    ) {
        validateDateRange(searchReq);

        Store store = getActiveStore(userId);

        List<ReportCancellationProjection> cancellations =
            reportMapper.findCancellations(
                store.getId(),
                searchReq
            );

        return cancellations.stream()
            .map(this::toReportCancellationResponse)
            .toList();
    }
    /**
     * 취소 유형별 요약 조회
     */
    @Transactional(readOnly = true)
    public List<ReportCancellationSummaryResponse> findCancellationSummary(
        Long userId,
        ReportSearchRequest searchReq
    ) {
        validateDateRange(searchReq);

        Store store = getActiveStore(userId);

        List<ReportCancellationSummaryProjection> summaries =
            reportMapper.findCancellationSummary(
                store.getId(),
                searchReq
            );

        return summaries.stream()
            .map(this::toReportCancellationSummaryResponse)
            .toList();
    }
    private ReportCancellationResponse toReportCancellationResponse(
        ReportCancellationProjection cancellation
    ) {
        return ReportCancellationResponse.builder()
            .id(cancellation.getId())
            .orderNo(cancellation.getOrderNo())
            .platformOrderNumber(
                cancellation.getPlatformOrderNumber()
            )
            .platformType(cancellation.getPlatformType())
            .previousStatus(cancellation.getPreviousStatus())
            .menuSummary(cancellation.getMenuSummary())
            .totalQuantity(cancellation.getTotalQuantity())
            .totalAmount(cancellation.getTotalAmount())
            .netProfit(cancellation.getNetProfit())
            .cancelType(cancellation.getCancelType())
            .cancelReason(cancellation.getCancelReason())
            .canceledByType(cancellation.getCanceledByType())
            .canceledByUserId(cancellation.getCanceledByUserId())
            .orderedAt(cancellation.getOrderedAt())
            .canceledAt(cancellation.getCanceledAt())
            .requestText(cancellation.getRequestText())
            .requestRiskType(cancellation.getRequestRiskType())
            .requestRiskLevel(cancellation.getRequestRiskLevel())
            .build();
    }
    private ReportCancellationSummaryResponse toReportCancellationSummaryResponse(
        ReportCancellationSummaryProjection summary
    ) {
        return ReportCancellationSummaryResponse.builder()
            .cancelType(summary.getCancelType())
            .cancelCount(
                getSafeLong(
                    summary.getCancelCount()
                )
            )
            .build();
    }
    
    private ReportPlatformResponse toReportPlatformResponse(
        ReportPlatformProjection platform
    ) {
        long totalOrderCount =
            getSafeLong(
                platform.getTotalOrderCount()
            );

        long completedOrderCount =
            getSafeLong(
                platform.getCompletedOrderCount()
            );

        long canceledOrderCount =
            getSafeLong(
                platform.getCanceledOrderCount()
            );

        long totalSales =
            getSafeLong(
                platform.getTotalSales()
            );

        long totalNetProfit =
            getSafeLong(
                platform.getTotalNetProfit()
            );

        double cancelRate =
            calculateCancelRate(
                totalOrderCount,
                canceledOrderCount
            );

        long averageOrderAmount =
            calculateAverageOrderAmount(
                completedOrderCount,
                totalSales
            );

        long averageNetProfit =
            calculateAverageNetProfit(
                completedOrderCount,
                totalNetProfit
            );

        return ReportPlatformResponse.builder()
            .platformType(platform.getPlatformType())
            .totalOrderCount(totalOrderCount)
            .completedOrderCount(completedOrderCount)
            .canceledOrderCount(canceledOrderCount)
            .totalSales(totalSales)
            .totalNetProfit(totalNetProfit)
            .cancelRate(cancelRate)
            .averageOrderAmount(averageOrderAmount)
            .averageNetProfit(averageNetProfit)
            .build();
    }
    private long calculateAverageNetProfit(
        long completedOrderCount,
        long totalNetProfit
    ) {
        if (completedOrderCount == 0) {
            return 0L;
        }

        return Math.round(
            (double) totalNetProfit / completedOrderCount
        );
    }
    private void validateDateRange(
        ReportSearchRequest searchReq
    ) {
        if (searchReq.startDate() == null
            || searchReq.endDate() == null) {
            return;
        }

        if (searchReq.startDate().isAfter(
            searchReq.endDate()
        )) {
            throw new BadRequestException(
                "시작일은 종료일보다 늦을 수 없습니다."
            );
        }
    }

    private double calculateCancelRate(
        long totalOrderCount,
        long canceledOrderCount
    ) {
        if (totalOrderCount == 0) {
            return 0.0;
        }

        double rawRate =
            ((double) canceledOrderCount / totalOrderCount) * 100;

        return Math.round(rawRate * 10) / 10.0;
    }

    private long calculateAverageOrderAmount(
        long completedOrderCount,
        long totalSales
    ) {
        if (completedOrderCount == 0) {
            return 0L;
        }

        return Math.round(
            (double) totalSales / completedOrderCount
        );
    }

    private long getSafeLong(
        Long value
    ) {
        return value == null ? 0L : value;
    }

    private Store getActiveStore(
        Long userId
    ) {
        Store store =
            storeMapper.findByUserId(userId);

        if (store == null) {
            throw new NotRegisteredStoreException(
                "등록된 활성 매장이 없습니다."
            );
        }

        return store;
    }
}