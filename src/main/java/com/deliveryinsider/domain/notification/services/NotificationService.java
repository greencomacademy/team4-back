package com.deliveryinsider.domain.notification.services;

import com.deliveryinsider.domain.notification.responses.HeaderNotificationResponse;
import com.deliveryinsider.domain.order.responses.OrderOperationSummaryResponse;
import com.deliveryinsider.domain.order.services.OrderOperationSummaryService;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final OrderOperationSummaryService orderOperationSummaryService;

    /**
     * 헤더 알림 목록을 현재 운영 요약 기준으로 생성한다.
     * 1차에서는 별도 알림 테이블 없이 주문 운영 요약 API 값을 재사용한다.
     */
    @Transactional(readOnly = true)
    public List<HeaderNotificationResponse> findHeaderNotifications(
            Long userId
    ) {
        try {
            OrderOperationSummaryResponse summary =
                    orderOperationSummaryService.findOperationSummary(userId);

            List<HeaderNotificationResponse> notifications =
                    new ArrayList<>();

            long requestRiskCount = getSafeLong(summary.requestRiskCount());
            long delayRiskCount = getSafeLong(summary.delayRiskCount());
            long lossRiskCount = getSafeLong(summary.lossRiskCount());

            if (requestRiskCount > 0) {
                notifications.add(HeaderNotificationResponse.builder()
                        .title("요구사항 확인 필요")
                        .description(requestRiskCount + "건의 주문에 알러지·분쟁 가능 표현이 있습니다.")
                        .path("/orders?attention=REQUEST")
                        .build());
            }

            if (delayRiskCount > 0) {
                notifications.add(HeaderNotificationResponse.builder()
                        .title("지연 위험 주문")
                        .description(delayRiskCount + "건의 주문이 조리 지연 위험 상태입니다.")
                        .path("/orders?attention=DELAY")
                        .build());
            }

            if (lossRiskCount > 0) {
                notifications.add(HeaderNotificationResponse.builder()
                        .title("손실 위험 주문")
                        .description(lossRiskCount + "건의 주문이 예상 순수익 확인 대상입니다.")
                        .path("/orders?attention=LOSS")
                        .build());
            }

            return notifications;
        } catch (DeletedRecordException e) {
            return List.of();
        }
    }

    private long getSafeLong(Long value) {
        return value == null ? 0L : value;
    }
}
