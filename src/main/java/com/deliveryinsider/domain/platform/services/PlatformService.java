package com.deliveryinsider.domain.platform.services;

import com.deliveryinsider.domain.platform.requests.PlatformUpdateRequest;
import com.deliveryinsider.domain.platform.entities.PlatformSetting;
import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.domain.platform.mapper.PlatformMapper;
import com.deliveryinsider.domain.platform.responses.PlatformResponse;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformService {

    private final PlatformMapper platformMapper;
    private final StoreMapper storeMapper;
    /**
     * 신규 매장에 고정 플랫폼 4개의 기본 정산 설정 생성
     */
    @Transactional(rollbackFor = Exception.class)
    public void createDefaults(Long storeId) {

        List<PlatformSetting> settings = List.of(
                createSetting(
                        storeId,
                        PlatformType.BAEMIN,
                        "6.80",
                        2600,
                        800
                ),
                createSetting(
                        storeId,
                        PlatformType.COUPANG_EATS,
                        "9.80",
                        3200,
                        1200
                ),
                createSetting(
                        storeId,
                        PlatformType.YOGIYO,
                        "8.50",
                        2800,
                        1000
                ),
                createSetting(
                        storeId,
                        PlatformType.DDANGYO,
                        "4.50",
                        1800,
                        500
                )
        );

        int result = platformMapper.saveAll(settings);

        if (result != settings.size()) {
            throw new RuntimeException(
                    "플랫폼 기본 설정 생성 중 문제가 발생했습니다."
            );
        }
    }

    /**
     * 플랫폼 기본 설정 객체 생성
     */
    private PlatformSetting createSetting(
            Long storeId,
            PlatformType platformType,
            String commissionRate,
            Integer deliveryFee,
            Integer couponCost
    ) {
        return PlatformSetting.builder()
                .storeId(storeId)
                .platformType(platformType)
                .commissionRate(new BigDecimal(commissionRate))
                .deliveryFee(deliveryFee)
                .couponCost(couponCost)
                .platformSupportAmount(0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PlatformResponse> findAll(Long userId) {

        // 1. 로그인한 사용자의 활성 매장 조회
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new DeletedRecordException(
                    "등록된 활성 매장이 없습니다."
            );
        }

        // 2. 해당 매장의 플랫폼 설정 전체 조회
        List<PlatformSetting> settings =
                platformMapper.findAllByStoreId(store.getId());

        // 3. Entity 목록을 응답 DTO 목록으로 변환
        return settings.stream()
                .map(this::toPlatformResponse)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public PlatformResponse update(
            Long userId,
            PlatformType platformType,
            PlatformUpdateRequest updateRequest
    ) {
        // 1. 로그인한 사용자의 활성 매장 조회
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new DeletedRecordException(
                    "등록된 활성 매장이 없습니다."
            );
        }

        // 2. 해당 매장에 수정 대상 플랫폼 설정이 존재하는지 조회
        PlatformSetting currentSetting =
                platformMapper.findByStoreIdAndPlatformType(
                        store.getId(),
                        platformType
                );

        if (currentSetting == null) {
            throw new DeletedRecordException(
                    "플랫폼 설정을 찾을 수 없습니다."
            );
        }

        /*
         * 3. UPDATE에 사용할 객체 생성
         *
         * id, storeId, platformType
         * → WHERE 절에서 사용
         *
         * 나머지 필드
         * → null이 아닌 값만 XML의 <if>에 포함
         */
        PlatformSetting updateSetting =
                PlatformSetting.builder()
                        .id(currentSetting.getId())
                        .storeId(store.getId())
                        .platformType(platformType)
                        .commissionRate(updateRequest.commissionRate())
                        .deliveryFee(updateRequest.deliveryFee())
                        .couponCost(updateRequest.couponCost())
                        .platformSupportAmount(
                                updateRequest.platformSupportAmount()
                        )
                        .build();

        // 4. DB 부분 수정
        int result =
                platformMapper.update(updateSetting);

        if (result != 1) {
            throw new RuntimeException(
                    "플랫폼 설정 수정 중 문제가 발생했습니다."
            );
        }

        // 5. 수정 결과 재조회
        PlatformSetting updatedSetting =
                platformMapper.findByStoreIdAndPlatformType(
                        store.getId(),
                        platformType
                );

        if (updatedSetting == null) {
            throw new RuntimeException(
                    "수정된 플랫폼 설정을 조회할 수 없습니다."
            );
        }

        return toPlatformResponse(updatedSetting);
    }

    private PlatformResponse toPlatformResponse(
            PlatformSetting setting
    ) {
        return PlatformResponse.builder()
                .id(setting.getId())
                .platformType(setting.getPlatformType())
                .commissionRate(setting.getCommissionRate())
                .deliveryFee(setting.getDeliveryFee())
                .couponCost(setting.getCouponCost())
                .platformSupportAmount(
                        setting.getPlatformSupportAmount()
                )
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}