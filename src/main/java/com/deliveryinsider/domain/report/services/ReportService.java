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


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
    /**
     * 운영 리포트 주문 목록 CSV 내보내기
     * 기존 주문 리포트 목록 조회 조건을 그대로 사용해서
     * 엑셀에서 열 수 있는 CSV 문자열을 생성한다.
     */
    @Transactional(readOnly = true)
    public String exportOrdersCsv(
        Long userId,
        ReportSearchRequest searchReq
    ) {
        List<ReportOrderResponse> orders =
            findOrders(
                userId,
                searchReq
            );

        StringBuilder csv = new StringBuilder();

        /*
         * UTF-8 BOM.
         * 엑셀에서 한글이 깨지지 않게 하기 위해 CSV 맨 앞에 붙인다.
         */
        csv.append('\uFEFF');

        appendCsvLine(
            csv,
            List.of(
                "주문ID",
                "플랫폼 주문번호",
                "플랫폼",
                "주문상태",
                "메뉴요약",
                "총수량",
                "배달주소",
                "주문금액",
                "플랫폼수수료",
                "배달비부담",
                "쿠폰부담",
                "플랫폼지원금",
                "메뉴원가",
                "포장비",
                "예상순수익",
                "주문일시",
                "조리시작일시",
                "완료일시",
                "취소일시",
                "환불일시",
                "요구사항",
                "요구사항 위험유형",
                "요구사항 위험등급",
                "요구사항 분석메시지",
                "취소유형",
                "취소사유",
                "환불유형",
                "환불사유"

            )
        );

        for (ReportOrderResponse order : orders) {
            appendCsvLine(
                csv,
                Arrays.asList(
                    order.id(),
                    order.platformOrderNumber(),
                    order.platformType(),
                    order.orderStatus(),
                    order.menuSummary(),
                    order.totalQuantity(),
                    order.deliveryAddress(),
                    order.totalAmount(),
                    order.commissionAmount(),
                    order.deliveryFee(),
                    order.couponCost(),
                    order.platformSupportAmount(),
                    order.totalMenuCost(),
                    order.totalPackagingFee(),
                    order.netProfit(),
                    formatDateTime(order.orderedAt()),
                    formatDateTime(order.cookingStartedAt()),
                    formatDateTime(order.completedAt()),
                    formatDateTime(order.canceledAt()),
                    formatDateTime(order.refundedAt()),
                    order.requestText(),
                    order.requestRiskType(),
                    order.requestRiskLevel(),
                    order.requestAnalysisMessage(),
                    order.cancelType(),
                    order.cancelReason(),
                    order.refundType(),
                    order.refundReason()
                )
            );
        }

        return csv.toString();
    }
    /**
     * CSV 한 줄 추가
     */
    private void appendCsvLine(
        StringBuilder csv,
        List<?> values
    ) {
        String line =
            values.stream()
                .map(this::escapeCsv)
                .reduce(
                    (left, right) -> left + "," + right
                )
                .orElse("");

        csv.append(line)
            .append(System.lineSeparator());
    }

    /**
     * CSV 값 escape 처리
     * 쉼표, 줄바꿈, 쌍따옴표가 들어가도
     * 엑셀에서 한 칸으로 읽히게 만든다.
     */
    private String escapeCsv(
        Object value
    ) {
        if (value == null) {
            return "";
        }

        String text =
            String.valueOf(value);

        String escapedText =
            text.replace("\"", "\"\"");

        return "\"" + escapedText + "\"";
    }

    /**
     * LocalDateTime 출력 형식 통일
     */
    private String formatDateTime(
        LocalDateTime dateTime
    ) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
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
            .commissionAmount(order.getCommissionAmount())
            .deliveryFee(order.getDeliveryFee())
            .couponCost(order.getCouponCost())
            .platformSupportAmount(order.getPlatformSupportAmount())
            .totalMenuCost(order.getTotalMenuCost())
            .totalPackagingFee(order.getTotalPackagingFee())
            .netProfit(order.getNetProfit())
            .orderedAt(order.getOrderedAt())
            .cookingStartedAt(order.getCookingStartedAt())
            .completedAt(order.getCompletedAt())
            .canceledAt(order.getCanceledAt())
            .refundedAt(order.getRefundedAt())
            .requestText(order.getRequestText())
            .requestRiskType(order.getRequestRiskType())
            .requestRiskLevel(order.getRequestRiskLevel())
            .requestAnalysisMessage(
                order.getRequestAnalysisMessage()
            )
            .cancelType(order.getCancelType())
            .cancelReason(order.getCancelReason())
                .refundType(order.getRefundType())
                .refundReason(order.getRefundReason())
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