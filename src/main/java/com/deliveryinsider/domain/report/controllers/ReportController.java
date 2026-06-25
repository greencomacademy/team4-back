package com.deliveryinsider.domain.report.controllers;

import com.deliveryinsider.domain.report.requests.ReportSearchRequest;
import com.deliveryinsider.domain.report.responses.*;
import com.deliveryinsider.domain.report.services.ReportService;
import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.nio.charset.StandardCharsets;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 운영 리포트 요약 조회
     * GET /api/reports/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<GlobalRes<ReportSummaryResponse>> findSummary(
        Authentication authentication,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        OrderStatus orderStatus,

        @RequestParam(required = false)
        String riskType,

        @RequestParam(required = false)
        String keyword
    ) {
        Long userId = extractUserId(authentication);

        ReportSearchRequest searchReq =
            ReportSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .platformType(platformType)
                .orderStatus(orderStatus)
                .riskType(riskType)
                .keyword(keyword)
                .build();

        ReportSummaryResponse result =
            reportService.findSummary(
                userId,
                searchReq
            );

        return ResponseEntity.ok(
            GlobalRes.<ReportSummaryResponse>builder()
                .code("00")
                .message("운영 리포트 요약 조회 성공")
                .data(result)
                .build()
        );
    }
    /**
     * 운영 리포트 주문 목록 조회
     * GET /api/reports/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<GlobalRes<List<ReportOrderResponse>>> findOrders(
        Authentication authentication,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        OrderStatus orderStatus,

        @RequestParam(required = false)
        String riskType,

        @RequestParam(required = false)
        String keyword
    ) {
        Long userId = extractUserId(authentication);

        ReportSearchRequest searchReq =
            ReportSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .platformType(platformType)
                .orderStatus(orderStatus)
                .riskType(riskType)
                .keyword(keyword)
                .build();

        List<ReportOrderResponse> result =
            reportService.findOrders(
                userId,
                searchReq
            );

        return ResponseEntity.ok(
            GlobalRes.<List<ReportOrderResponse>>builder()
                .code("00")
                .message("운영 리포트 주문 목록 조회 성공")
                .data(result)
                .build()
        );
    }
    /**
     * 플랫폼별 리포트 조회
     * GET /api/reports/platforms
     */
    @GetMapping("/platforms")
    public ResponseEntity<GlobalRes<List<ReportPlatformResponse>>> findPlatforms(
        Authentication authentication,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        OrderStatus orderStatus,

        @RequestParam(required = false)
        String riskType,

        @RequestParam(required = false)
        String keyword
    ) {
        Long userId = extractUserId(authentication);

        ReportSearchRequest searchReq =
            ReportSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .platformType(platformType)
                .orderStatus(orderStatus)
                .riskType(riskType)
                .keyword(keyword)
                .build();

        List<ReportPlatformResponse> result =
            reportService.findPlatforms(
                userId,
                searchReq
            );

        return ResponseEntity.ok(
            GlobalRes.<List<ReportPlatformResponse>>builder()
                .code("00")
                .message("플랫폼별 리포트 조회 성공")
                .data(result)
                .build()
        );
    }
    /**
     * 운영 리포트 주문 목록 CSV 내보내기
     * GET /api/reports/orders/export
     */
    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders(
        Authentication authentication,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        OrderStatus orderStatus,

        @RequestParam(required = false)
        String riskType,

        @RequestParam(required = false)
        String keyword
    ) {
        Long userId = extractUserId(authentication);

        ReportSearchRequest searchReq =
            ReportSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .platformType(platformType)
                .orderStatus(orderStatus)
                .riskType(riskType)
                .keyword(keyword)
                .build();

        String csv =
            reportService.exportOrdersCsv(
                userId,
                searchReq
            );

        byte[] csvBytes =
            csv.getBytes(StandardCharsets.UTF_8);

        String fileName =
            "DeliveryInsider_orders_report.csv";

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .contentType(
                MediaType.parseMediaType("text/csv; charset=UTF-8")
            )
            .body(csvBytes);
    }
    
    
    
    /**
     * 취소 리포트 목록 조회
     * GET /api/reports/cancellations
     */
    @GetMapping("/cancellations")
    public ResponseEntity<GlobalRes<List<ReportCancellationResponse>>> findCancellations(
        Authentication authentication,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        String riskType,

        @RequestParam(required = false)
        String keyword
    ) {
        Long userId = extractUserId(authentication);

        ReportSearchRequest searchReq =
            ReportSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .platformType(platformType)
                .riskType(riskType)
                .keyword(keyword)
                .build();

        List<ReportCancellationResponse> result =
            reportService.findCancellations(
                userId,
                searchReq
            );

        return ResponseEntity.ok(
            GlobalRes.<List<ReportCancellationResponse>>builder()
                .code("00")
                .message("취소 리포트 목록 조회 성공")
                .data(result)
                .build()
        );
    }
    /**
     * 취소 유형별 요약 조회
     * GET /api/reports/cancellations/summary
     */
    @GetMapping("/cancellations/summary")
    public ResponseEntity<GlobalRes<List<ReportCancellationSummaryResponse>>> findCancellationSummary(
        Authentication authentication,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @RequestParam(required = false)
        PlatformType platformType,

        @RequestParam(required = false)
        String riskType,

        @RequestParam(required = false)
        String keyword
    ) {
        Long userId = extractUserId(authentication);

        ReportSearchRequest searchReq =
            ReportSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .platformType(platformType)
                .riskType(riskType)
                .keyword(keyword)
                .build();

        List<ReportCancellationSummaryResponse> result =
            reportService.findCancellationSummary(
                userId,
                searchReq
            );

        return ResponseEntity.ok(
            GlobalRes.<List<ReportCancellationSummaryResponse>>builder()
                .code("00")
                .message("취소 유형별 요약 조회 성공")
                .data(result)
                .build()
        );
    }
    
    private Long extractUserId(
        Authentication authentication
    ) {
        Claims claims =
            (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}
