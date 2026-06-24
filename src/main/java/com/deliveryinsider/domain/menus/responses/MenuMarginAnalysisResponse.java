package com.deliveryinsider.domain.menus.responses;

import com.deliveryinsider.domain.menus.enums.CookingBurdenLevel;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MenuMarginAnalysisResponse(

        // 메뉴 PK
        Long menuId,

        // 메뉴명
        String menuName,

        // 판매 가격
        Integer menuPrice,

        // 메뉴 원가
        Integer menuCost,

        // 포장 비용
        Integer packagingFee,

        // 기본 예상 조리시간
        Integer expectedCookingTime,

        // 한 번에 조리 가능한 수량
        Integer batchCapacity,

        // 판매가 - 원가 - 포장비
        Integer expectedMargin,

        // 예상 마진 ÷ 판매가 × 100
        BigDecimal expectedMarginRate,

        // 예상 조리시간 ÷ 동시 조리 가능 수량
        BigDecimal cookingBurdenScore,

        // LOW, NORMAL, HIGH, OVERLOAD
        CookingBurdenLevel cookingBurdenLevel,

        // 메뉴별 운영 분석 문구
        String summary

) {
}