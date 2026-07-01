package com.deliveryinsider.domain.menus.mapper;

import com.deliveryinsider.domain.menus.entities.Menu;
import com.deliveryinsider.domain.menus.entities.MenuLossDismissal;
import com.deliveryinsider.domain.menus.projections.MenuSalesQuantityProjection;
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

    // 완료 주문 기준 메뉴별 플랫폼 판매 수량 조회
    List<MenuSalesQuantityProjection> findSalesQuantityByStoreId(
            @Param("storeId") Long storeId
    );

    // 숨은 손실 메뉴 확인 완료 목록 조회
    List<MenuLossDismissal> findActiveLossDismissalsByStoreId(
            @Param("storeId") Long storeId
    );

    // 특정 메뉴 숨김 정보 조회
    MenuLossDismissal findLossDismissalByStoreIdAndMenuId(
            @Param("storeId") Long storeId,
            @Param("menuId") Long menuId
    );

    // 숨은 손실 메뉴 확인 완료 저장 또는 갱신
    int upsertLossDismissal(MenuLossDismissal dismissal);

    // 확인 완료 숨김 해제
    int restoreLossDismissal(
            @Param("storeId") Long storeId,
            @Param("menuId") Long menuId
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