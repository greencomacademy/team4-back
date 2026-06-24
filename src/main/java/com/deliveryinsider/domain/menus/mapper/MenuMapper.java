package com.deliveryinsider.domain.menus.mapper;

import com.deliveryinsider.domain.menus.entities.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    // 메뉴 등록
    int save(Menu menu);

    // 로그인한 사용자의 활성 매장에 소속된 활성 메뉴 전체 조회
    List<Menu> findAllByStoreId(
            @Param("storeId") Long storeId
    );

    // 매장 소유권까지 확인하며 메뉴 한 건 조회
    Menu findByIdAndStoreId(
            @Param("id") Long id,
            @Param("storeId") Long storeId
    );

    // 전달된 필드만 부분 수정
    int update(Menu menu);

    // 메뉴 소프트 삭제
    int softDelete(
            @Param("id") Long id,
            @Param("storeId") Long storeId
    );
}