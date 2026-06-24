package com.deliveryinsider.domain.menus.services;

import com.deliveryinsider.domain.menus.enums.CookingBurdenLevel;
import com.deliveryinsider.domain.menus.responses.MenuMarginAnalysisResponse;
import com.deliveryinsider.domain.menus.entities.Menu;
import com.deliveryinsider.domain.menus.mapper.MenuMapper;
import com.deliveryinsider.domain.menus.requests.MenuCreateRequest;
import com.deliveryinsider.domain.menus.requests.MenuUpdateRequest;
import com.deliveryinsider.domain.menus.responses.MenuResponse;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.errors.custom.DeletedRecordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;
    private final StoreMapper storeMapper;

    /**
     * 메뉴 등록
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResponse create(
            Long userId,
            MenuCreateRequest createReq
    ) {
        // 1. 로그인 사용자의 활성 매장 조회
        Store store = getActiveStore(userId);

        // 2. 요청 DTO를 Menu 엔티티로 변환
        Menu menu = Menu.builder()
                .storeId(store.getId())
                .menuName(createReq.menuName())
                .menuPrice(createReq.menuPrice())
                .menuCost(createReq.menuCost())
                .packagingFee(createReq.packagingFee())
                .expectedCookingTime(
                        createReq.expectedCookingTime()
                )
                .batchCapacity(createReq.batchCapacity())
                .build();

        // 3. DB 저장
        int result = menuMapper.save(menu);

        if (result != 1) {
            throw new RuntimeException(
                    "메뉴 등록 중 문제가 발생했습니다."
            );
        }

        // 4. useGeneratedKeys로 생성된 menu.id를 사용해 재조회
        Menu savedMenu = menuMapper.findByIdAndStoreId(
                menu.getId(),
                store.getId()
        );

        if (savedMenu == null) {
            throw new RuntimeException(
                    "등록된 메뉴를 조회할 수 없습니다."
            );
        }

        // 5. 계산 결과를 포함한 응답 DTO 반환
        return toMenuRes(savedMenu);
    }

    /**
     * 내 활성 매장의 메뉴 전체 조회
     */
    @Transactional(readOnly = true)
    public List<MenuResponse> findAll(Long userId) {
        Store store = getActiveStore(userId);

        List<Menu> menus =
                menuMapper.findAllByStoreId(store.getId());

        return menus.stream()
                .map(this::toMenuRes)
                .toList();
    }
    /**
     * 내 활성 매장의 메뉴별 마진 및 조리 부담 분석
     */
    @Transactional(readOnly = true)
    public List<MenuMarginAnalysisResponse> findMarginAnalysis(
            Long userId
    ) {
        /*
         * 기존 메뉴 전체 조회를 재사용한다.
         *
         * MenuRes에는 이미 예상 마진과
         * 예상 마진율이 계산되어 있다.
         */
        List<MenuResponse> menus = findAll(userId);

        return menus.stream()
                .map(this::toMenuMarginAnalysisRes)
                .toList();
    }

    /**
     * 내 활성 매장의 메뉴 상세 조회
     */
    @Transactional(readOnly = true)
    public MenuResponse findOne(
            Long userId,
            Long menuId
    ) {
        Store store = getActiveStore(userId);

        Menu menu = menuMapper.findByIdAndStoreId(
                menuId,
                store.getId()
        );

        if (menu == null) {
            throw new DeletedRecordException(
                    "메뉴를 찾을 수 없습니다."
            );
        }

        return toMenuRes(menu);
    }

    /**
     * 메뉴 부분 수정
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResponse update(
            Long userId,
            Long menuId,
            MenuUpdateRequest updateReq
    ) {
        // 1. 사용자의 활성 매장 조회
        Store store = getActiveStore(userId);

        // 2. 해당 매장의 활성 메뉴 조회
        Menu currentMenu =
                menuMapper.findByIdAndStoreId(
                        menuId,
                        store.getId()
                );

        if (currentMenu == null) {
            throw new DeletedRecordException(
                    "수정할 메뉴를 찾을 수 없습니다."
            );
        }

        // 3. 요청값이 존재하고 기존 값과 다른 필드만 추출
        String changedMenuName = getChangedValue(
                currentMenu.getMenuName(),
                updateReq.menuName()
        );

        Integer changedMenuPrice = getChangedValue(
                currentMenu.getMenuPrice(),
                updateReq.menuPrice()
        );

        Integer changedMenuCost = getChangedValue(
                currentMenu.getMenuCost(),
                updateReq.menuCost()
        );

        Integer changedPackagingFee = getChangedValue(
                currentMenu.getPackagingFee(),
                updateReq.packagingFee()
        );

        Integer changedExpectedCookingTime =
                getChangedValue(
                        currentMenu.getExpectedCookingTime(),
                        updateReq.expectedCookingTime()
                );

        Integer changedBatchCapacity = getChangedValue(
                currentMenu.getBatchCapacity(),
                updateReq.batchCapacity()
        );

        // 4. 요청은 있었지만 실제 값이 전부 동일한 경우
        boolean hasChangedValue =
                changedMenuName != null
                        || changedMenuPrice != null
                        || changedMenuCost != null
                        || changedPackagingFee != null
                        || changedExpectedCookingTime != null
                        || changedBatchCapacity != null;

        if (!hasChangedValue) {
            return toMenuRes(currentMenu);
        }

        // 5. 동적 UPDATE용 Menu 객체 생성
        Menu updateMenu = Menu.builder()
                .id(currentMenu.getId())
                .storeId(store.getId())
                .menuName(changedMenuName)
                .menuPrice(changedMenuPrice)
                .menuCost(changedMenuCost)
                .packagingFee(changedPackagingFee)
                .expectedCookingTime(
                        changedExpectedCookingTime
                )
                .batchCapacity(changedBatchCapacity)
                .build();

        // 6. DB 수정
        int result = menuMapper.update(updateMenu);

        if (result != 1) {
            throw new RuntimeException(
                    "메뉴 수정 중 문제가 발생했습니다."
            );
        }

        // 7. 수정 결과 재조회
        Menu updatedMenu =
                menuMapper.findByIdAndStoreId(
                        menuId,
                        store.getId()
                );

        if (updatedMenu == null) {
            throw new RuntimeException(
                    "수정된 메뉴를 조회할 수 없습니다."
            );
        }

        return toMenuRes(updatedMenu);
    }

    /**
     * 메뉴 소프트 삭제
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(
            Long userId,
            Long menuId
    ) {
        Store store = getActiveStore(userId);

        int result = menuMapper.softDelete(
                menuId,
                store.getId()
        );

        if (result != 1) {
            throw new DeletedRecordException(
                    "삭제할 메뉴를 찾을 수 없습니다."
            );
        }
    }

    /**
     * 로그인 사용자의 활성 매장 조회
     */
    private Store getActiveStore(Long userId) {
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new DeletedRecordException(
                    "등록된 활성 매장이 없습니다."
            );
        }

        return store;
    }

    /**
     * 기존 값과 요청값 비교
     * 요청하지 않은 필드거나 기존 값과 같으면 null 반환
     */
    private <T> T getChangedValue(
            T currentValue,
            T requestedValue
    ) {
        if (requestedValue == null) {
            return null;
        }

        if (Objects.equals(currentValue, requestedValue)) {
            return null;
        }

        return requestedValue;
    }

    /**
     * Menu 엔티티를 MenuRes로 변환하면서 마진 계산
     */
    private MenuResponse toMenuRes(Menu menu) {
        int expectedMargin =
                menu.getMenuPrice()
                        - menu.getMenuCost()
                        - menu.getPackagingFee();

        BigDecimal expectedMarginRate;

        // 판매가가 0원이면 나눗셈을 할 수 없으므로 0% 처리
        if (menu.getMenuPrice() == 0) {
            expectedMarginRate = BigDecimal.ZERO;
        } else {
            expectedMarginRate =
                    BigDecimal.valueOf(expectedMargin)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    BigDecimal.valueOf(
                                            menu.getMenuPrice()
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        return MenuResponse.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .menuPrice(menu.getMenuPrice())
                .menuCost(menu.getMenuCost())
                .packagingFee(menu.getPackagingFee())
                .expectedCookingTime(
                        menu.getExpectedCookingTime()
                )
                .batchCapacity(menu.getBatchCapacity())
                .expectedMargin(expectedMargin)
                .expectedMarginRate(expectedMarginRate)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
    /**
     * MenuRes를 메뉴별 마진 분석 응답으로 변환한다.
     */
    private MenuMarginAnalysisResponse toMenuMarginAnalysisRes(
            MenuResponse menu
    ) {
        BigDecimal cookingBurdenScore =
                calculateCookingBurdenScore(
                        menu.expectedCookingTime(),
                        menu.batchCapacity()
                );

        CookingBurdenLevel cookingBurdenLevel =
                determineCookingBurdenLevel(
                        cookingBurdenScore
                );

        String summary =
                createMarginAnalysisSummary(
                        menu.expectedMargin(),
                        menu.expectedMarginRate(),
                        cookingBurdenLevel
                );

        return MenuMarginAnalysisResponse.builder()
                .menuId(menu.id())
                .menuName(menu.menuName())
                .menuPrice(menu.menuPrice())
                .menuCost(menu.menuCost())
                .packagingFee(menu.packagingFee())
                .expectedCookingTime(
                        menu.expectedCookingTime()
                )
                .batchCapacity(menu.batchCapacity())
                .expectedMargin(menu.expectedMargin())
                .expectedMarginRate(
                        menu.expectedMarginRate()
                )
                .cookingBurdenScore(
                        cookingBurdenScore
                )
                .cookingBurdenLevel(
                        cookingBurdenLevel
                )
                .summary(summary)
                .build();
    }

    /**
     * 조리 부담 점수 계산
     *
     * 예상 조리시간 / 한 번에 조리 가능한 수량
     */
    private BigDecimal calculateCookingBurdenScore(
            Integer expectedCookingTime,
            Integer batchCapacity
    ) {
        if (
                expectedCookingTime == null
                        || expectedCookingTime <= 0
        ) {
            throw new IllegalStateException(
                    "메뉴의 예상 조리시간이 올바르지 않습니다."
            );
        }

        if (
                batchCapacity == null
                        || batchCapacity <= 0
        ) {
            throw new IllegalStateException(
                    "메뉴의 동시 조리 가능 수량이 올바르지 않습니다."
            );
        }

        return BigDecimal
                .valueOf(expectedCookingTime)
                .divide(
                        BigDecimal.valueOf(batchCapacity),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * 조리 부담 점수에 따라 단계를 판정한다.
     */
    private CookingBurdenLevel determineCookingBurdenLevel(
            BigDecimal cookingBurdenScore
    ) {
        if (
                cookingBurdenScore.compareTo(
                        BigDecimal.valueOf(3)
                ) <= 0
        ) {
            return CookingBurdenLevel.LOW;
        }

        if (
                cookingBurdenScore.compareTo(
                        BigDecimal.valueOf(6)
                ) <= 0
        ) {
            return CookingBurdenLevel.NORMAL;
        }

        if (
                cookingBurdenScore.compareTo(
                        BigDecimal.valueOf(12)
                ) <= 0
        ) {
            return CookingBurdenLevel.HIGH;
        }

        return CookingBurdenLevel.OVERLOAD;
    }

    /**
     * 수익성과 조리 부담을 조합해
     * 메뉴별 운영 안내 문구를 생성한다.
     */
    private String createMarginAnalysisSummary(
            Integer expectedMargin,
            BigDecimal expectedMarginRate,
            CookingBurdenLevel cookingBurdenLevel
    ) {
        String marginMessage;

        if (expectedMargin < 0) {
            marginMessage =
                    "판매할수록 손실이 발생하는 메뉴입니다. "
                            + "가격이나 원가를 조정해 주세요.";
        } else if (
                expectedMarginRate.compareTo(
                        BigDecimal.valueOf(45)
                ) >= 0
        ) {
            marginMessage =
                    "예상 마진율이 높은 메뉴입니다.";
        } else if (
                expectedMarginRate.compareTo(
                        BigDecimal.valueOf(30)
                ) >= 0
        ) {
            marginMessage =
                    "예상 수익성이 무난한 메뉴입니다.";
        } else {
            marginMessage =
                    "예상 마진율이 낮아 가격이나 원가 확인이 필요합니다.";
        }

        String burdenMessage =
                switch (cookingBurdenLevel) {
                    case LOW ->
                            " 조리 부담이 낮아 피크 시간에도 운영하기 좋습니다.";

                    case NORMAL ->
                            " 조리 부담은 일반적인 수준입니다.";

                    case HIGH ->
                            " 피크 시간대에는 조리 지연 가능성을 확인해 주세요.";

                    case OVERLOAD ->
                            " 조리 부담이 매우 높아 주방 병목을 만들 수 있습니다.";
                };

        return marginMessage + burdenMessage;
    }
}