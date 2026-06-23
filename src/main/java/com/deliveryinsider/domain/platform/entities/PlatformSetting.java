package com.deliveryinsider.domain.platform.entities;

import com.deliveryinsider.global.enums.PlatformType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSetting {
    private Long id;

    // 이 플랫폼 설정을 소유한 매장 PK
    private Long storeId;

    private PlatformType platformType;

    // 중개이용료율(%)
    private BigDecimal commissionRate;

    // 점주 부담 배달비
    private Integer deliveryFee;

    // 점주 부담 쿠폰 금액
    private Integer couponCost;

    // 플랫폼이 지원하는 금액
    private Integer platformSupportAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}