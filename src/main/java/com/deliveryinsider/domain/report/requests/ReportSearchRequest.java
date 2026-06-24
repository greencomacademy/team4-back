package com.deliveryinsider.domain.report.requests;

import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.enums.PlatformType;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 운영 리포트 검색 조건
 * Controller의 query parameter를 Service/Mapper로 전달하기 위한 객체다.
 */
@Builder
public record ReportSearchRequest(

    LocalDate startDate,

    LocalDate endDate,

    PlatformType platformType,

    OrderStatus orderStatus,

    String riskType,

    String keyword

) {
}