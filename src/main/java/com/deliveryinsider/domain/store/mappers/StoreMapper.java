package com.deliveryinsider.domain.store.mappers;

import com.deliveryinsider.domain.store.entities.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoreMapper {

    // 해당 회원이 이미 매장을 등록했는지 확인
    int countByUserId(@Param("userId") Long userId);

    // 사업자등록번호 중복 확인
    int countByBusinessNumber(
            @Param("businessNumber") String businessNumber
    );

    // 매장 등록
    int save(Store store);

    // PK로 매장 조회
    Store findById(@Param("id") Long id);

    // 로그인한 회원의 매장 조회
    Store findByUserId(@Param("userId") Long userId);

    int update(Store store);

    int countByBusinessNumberExceptStoreId(
            @Param("businessNumber") String businessNumber,
            @Param("storeId") Long storeId
    );

    int softDeleteByUserId(@Param("userId") Long userId);
}