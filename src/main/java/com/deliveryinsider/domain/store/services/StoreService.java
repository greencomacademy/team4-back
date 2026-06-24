package com.deliveryinsider.domain.store.services;


import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.enums.BusinessStatus;
import com.deliveryinsider.domain.store.enums.OperationStatus;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.domain.store.requests.StoreCreateReq;
import com.deliveryinsider.domain.store.requests.StoreUpdateReq;
import com.deliveryinsider.domain.store.responses.StoreRes;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import com.deliveryinsider.global.errors.custom.DuplicatedRecordException;
import com.deliveryinsider.global.errors.custom.NotRegisteredException;
import com.deliveryinsider.global.errors.custom.NotRegisteredStoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreMapper storeMapper;
//    private final PlatformSettingService platformSettingService;

    /**
     * 매장 등록
     */
    @Transactional(rollbackFor = Exception.class)
    public StoreRes create(
            Long userId,
            StoreCreateReq storeCreateReq
    ) {
        // 1. 로그인 회원에게 활성 매장이 이미 있는지 확인
        int storeCount = storeMapper.countByUserId(userId);

        if (storeCount > 0) {
            throw new DuplicatedRecordException(
                    "이미 등록된 매장이 있습니다."
            );
        }

        // 2. 활성 상태인 같은 사업자등록번호가 있는지 확인
        int businessNumberCount =
                storeMapper.countByBusinessNumber(
                        storeCreateReq.businessNumber()
                );

        if (businessNumberCount > 0) {
            throw new DuplicatedRecordException(
                    "이미 사용 중인 사업자등록번호입니다."
            );
        }

        // 3. 요청 DTO와 JWT 사용자 번호를 Store 엔티티로 변환
        Store store = Store.builder()
                .userId(userId)
                .storeName(storeCreateReq.storeName())
                .phone(storeCreateReq.phone())
                .businessNumber(storeCreateReq.businessNumber())
                .businessStatus(BusinessStatus.PENDING)
                .businessVerifiedAt(null)
                .address(storeCreateReq.address())
                .addressDetail(storeCreateReq.addressDetail())
                .industryType(storeCreateReq.industryType())
                .operationStatus(
                    storeCreateReq.operationStatus() == null
                    ? OperationStatus.OPERATING
                    : storeCreateReq.operationStatus())
                .kitchenCapacity(storeCreateReq.kitchenCapacity())
                .openTime(storeCreateReq.openTime())
                .closeTime(storeCreateReq.closeTime())
                .phone(storeCreateReq.phone())
                .build();

        // 4. DB 저장
        int result = storeMapper.save(store);

        if (result != 1) {
            throw new RuntimeException(
                    "매장 등록 중 문제가 발생했습니다."
            );
        }
        // 새로 생성된 store.id를 이용해 플랫폼 기본 설정 4개 생성
//        platformSettingService.createDefaults(store.getId());

        // useGeneratedKeys로 생성된 PK가 store.id에 들어감
        Store savedStore = storeMapper.findById(store.getId());

        if (savedStore == null) {
            throw new RuntimeException(
                    "등록된 매장 정보를 조회할 수 없습니다."
            );
        }

        return toStoreRes(savedStore);
    }

    /**
     * 로그인한 회원의 활성 매장 조회
     */
    @Transactional(readOnly = true)
    public StoreRes findMyStore(Long userId) {
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new NotRegisteredStoreException(
                    "등록된 매장이 없습니다."
            );
        }

        return toStoreRes(store);
    }

    /**
     * Store 엔티티를 응답 DTO로 변환
     */
    private StoreRes toStoreRes(Store store) {
        return StoreRes.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .phone(store.getPhone())
                .businessNumber(store.getBusinessNumber())
                .businessStatus(store.getBusinessStatus())
                .businessVerifiedAt(store.getBusinessVerifiedAt())
                .address(store.getAddress())
                .addressDetail(store.getAddressDetail())
                .industryType(store.getIndustryType())
                .kitchenCapacity(store.getKitchenCapacity())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .operationStatus(store.getOperationStatus())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
    @Transactional(rollbackFor = Exception.class)
    public StoreRes update(
            Long userId,
            StoreUpdateReq storeUpdateReq
    ) {
        // 1. 로그인한 사용자의 활성 매장 조회
        Store currentStore = storeMapper.findByUserId(userId);

        if (currentStore == null) {
            throw new NotRegisteredStoreException(
                    "수정할 매장이 없습니다."
            );
        }

        /*
         * 2. 사업자번호가 요청에 포함됐고,
         * 기존 사업자번호와 실제로 다른 경우에만 중복 검사
         */
        String changedBusinessNumber = null;

        if (
                storeUpdateReq.businessNumber() != null
                        && !Objects.equals(
                        currentStore.getBusinessNumber(),
                        storeUpdateReq.businessNumber()
                )
        ) {
            int duplicateCount =
                    storeMapper.countByBusinessNumberExceptStoreId(
                            storeUpdateReq.businessNumber(),
                            currentStore.getId()
                    );

            if (duplicateCount > 0) {
                throw new DuplicatedRecordException(
                        "이미 사용 중인 사업자등록번호입니다."
                );
            }

            changedBusinessNumber =
                    storeUpdateReq.businessNumber();
        }

        /*
         * 3. 요청에 포함됐으면서 기존 값과 다른 필드만 추출
         * 값이 같거나 요청에 포함되지 않았다면 null
         */
        String changedStoreName = getChangedValue(
                currentStore.getStoreName(),
                storeUpdateReq.storeName()
        );

        String changePhone = getChangedValue(
                currentStore.getPhone(),
                storeUpdateReq.phone()
        );

        String changedAddress = getChangedValue(
                currentStore.getAddress(),
                storeUpdateReq.address()
        );
        
        String changedPhone = getChangedValue(
            currentStore.getPhone(),
            storeUpdateReq.phone()
        );

        String changedAddressDetail = getChangedValue(
                currentStore.getAddressDetail(),
                storeUpdateReq.addressDetail()
        );

        BusinessStatus changeBusinessStatus = getChangedValue(
                currentStore.getBusinessStatus(),
                storeUpdateReq.businessStatus() != null ? BusinessStatus.valueOf(storeUpdateReq.businessStatus()):null
        );

        String changedIndustryType = getChangedValue(
                currentStore.getIndustryType(),
                storeUpdateReq.industryType()
        );

        Integer changedKitchenCapacity = getChangedValue(
                currentStore.getKitchenCapacity(),
                storeUpdateReq.kitchenCapacity()
        );
        LocalTime changedOpenTime = getChangedValue(
                currentStore.getOpenTime(),
                storeUpdateReq.openTime()
        );

        LocalTime changedCloseTime = getChangedValue(
                currentStore.getCloseTime(),
                storeUpdateReq.closeTime()
        );
        OperationStatus changedOperationStatus = getChangedValue(
            currentStore.getOperationStatus(),
            storeUpdateReq.operationStatus()
        );

        // 4. 실제 변경된 값이 하나도 없다면 UPDATE하지 않고 현재 정보 반환
        boolean hasChangedValue =
                changedStoreName != null
                        || changedPhone != null
                        || changedBusinessNumber != null
                        || changedAddress != null
                        || changedAddressDetail != null
                        || changedIndustryType != null
                        || changedKitchenCapacity != null
                        || changedOpenTime != null
                        || changedCloseTime != null
                        || changeBusinessStatus != null;
                        || changedOperationStatus != null;

        if (!hasChangedValue) {
            return toStoreRes(currentStore);
        }

        /*
         * 5. 동적 UPDATE에 사용할 Store 객체 생성
         *
         * id, userId:
         * WHERE 절에서 사용
         *
         * 나머지 필드:
         * null이 아닌 값만 MyBatis <if>에서 UPDATE에 포함
         */
        Store updateStore = Store.builder()
                .id(currentStore.getId())
                .phone(changedPhone)
                .userId(userId)
                .storeName(changedStoreName)
                .phone(changePhone)
                .businessNumber(changedBusinessNumber)
                .businessStatus(changeBusinessStatus)
                .address(changedAddress)
                .addressDetail(changedAddressDetail)
                .industryType(changedIndustryType)
                .kitchenCapacity(changedKitchenCapacity)
                .openTime(changedOpenTime)
                .closeTime(changedCloseTime)
                .operationStatus(changedOperationStatus)
                .build();

        // 6. DB 수정
        int result = storeMapper.update(updateStore);

        if (result != 1) {
            throw new RuntimeException(
                    "매장 수정 중 문제가 발생했습니다."
            );
        }

        // 7. 수정된 매장을 다시 조회
        Store updatedStore =
                storeMapper.findByUserId(userId);

        if (updatedStore == null) {
            throw new RuntimeException(
                    "수정된 매장 정보를 조회할 수 없습니다."
            );
        }

        return toStoreRes(updatedStore);
    }

    /**
     * 로그인한 사용자의 활성 매장 소프트 삭제
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        int result = storeMapper.softDeleteByUserId(userId);
        if (result != 1){
            throw new DeletedRecordException("삭제할 매장이 없습니다.");
        }
    }




    private <T> T getChangedValue(
            T currentValue,
            T requestedValue
    ) {
        // 요청에 필드가 없으면 수정하지 않음
        if (requestedValue == null) {
            return null;
        }

        // 기존 값과 같아도 수정하지 않음
        if (Objects.equals(currentValue, requestedValue)) {
            return null;
        }

        return requestedValue;
    }


}