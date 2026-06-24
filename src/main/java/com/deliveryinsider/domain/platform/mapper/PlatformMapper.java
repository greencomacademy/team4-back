package com.deliveryinsider.domain.platform.mapper;

import com.deliveryinsider.domain.platform.entities.PlatformSetting;
import com.deliveryinsider.global.enums.PlatformType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlatformMapper {
    // 매장 생성 시 플랫폼 기본 설정 4개 일괄 저장
    int saveAll(
            @Param("settings") List<PlatformSetting> settings
    );

    // 로그인한 사용자의 매장에 등록된 플랫폼 설정 전체 조회
    List<PlatformSetting> findAllByStoreId(
            @Param("storeId") Long storeId
    );

    // 특정 매장의 특정 플랫폼 설정 조회
    PlatformSetting findByStoreIdAndPlatformType(
            @Param("storeId") Long storeId,
            @Param("platformType") PlatformType platformType
    );

    // 특정 플랫폼의 정산 조건 부분 수정
    int update(PlatformSetting platformSetting);

}