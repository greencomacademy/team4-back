package com.deliveryinsider.domain.mock.services;

import com.deliveryinsider.domain.menus.entities.Menu;
import com.deliveryinsider.domain.menus.mapper.MenuMapper;
import com.deliveryinsider.domain.mock.requests.MockOrderCreateRequest;
import com.deliveryinsider.domain.mock.responses.MockOrderCreateResponse;
import com.deliveryinsider.domain.mock.responses.MockOrderDeleteResponse;
import com.deliveryinsider.domain.order.entities.Order;
import com.deliveryinsider.domain.order.entities.OrderItem;
import com.deliveryinsider.domain.order.enums.MockOrderScenario;
import com.deliveryinsider.domain.order.mapper.OrderItemMapper;
import com.deliveryinsider.domain.order.mapper.OrderMapper;
import com.deliveryinsider.domain.platform.entities.PlatformSetting;
import com.deliveryinsider.domain.platform.mapper.PlatformMapper;
import com.deliveryinsider.domain.store.entities.Store;
import com.deliveryinsider.domain.store.mappers.StoreMapper;
import com.deliveryinsider.global.enums.OrderStatus;
import com.deliveryinsider.global.errors.custom.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MockOrderService {

    private final StoreMapper storeMapper;
    private final MenuMapper menuMapper;
    private final PlatformMapper platformSettingMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    /**
     * Mock 주문 여러 건 생성
     *
     * 한 건이라도 생성에 실패하면 전체 주문을 롤백한다.
     */
    @Transactional(rollbackFor = Exception.class)
    public MockOrderCreateResponse create(
            Long userId,
            MockOrderCreateRequest createReq
    ) {
        int requestedCount = createReq.resolvedCount();
        MockOrderScenario scenario =
                createReq.resolvedScenario();

        // 1. 로그인 회원의 활성 매장 확인
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new BadRequestException(
                    "Mock 주문을 생성할 활성 매장이 없습니다."
            );
        }

        // 2. Mock 주문에서 사용할 활성 메뉴 조회
        List<Menu> menus =
                menuMapper.findAllByStoreId(store.getId());

        if (menus.isEmpty()) {
            throw new BadRequestException(
                    "Mock 주문을 생성하려면 메뉴를 하나 이상 등록해야 합니다."
            );
        }

        // 3. 주문 비용 계산에 사용할 플랫폼 설정 조회
        List<PlatformSetting> platformSettings =
                platformSettingMapper.findAllByStoreId(
                        store.getId()
                );

        if (platformSettings.isEmpty()) {
            throw new BadRequestException(
                    "플랫폼 설정이 존재하지 않습니다."
            );
        }

        // 4. 요청받은 개수만큼 주문 생성
        List<Order> createdOrders = new ArrayList<>();

        for (int i = 0; i < requestedCount; i++) {
            Order createdOrder = createOneOrder(
                    store,
                    menus,
                    platformSettings,
                    scenario
            );

            createdOrders.add(createdOrder);
        }

        // 5. 생성 결과 응답
        return MockOrderCreateResponse.builder()
                .requestedCount(requestedCount)
                .createdCount(createdOrders.size())
                .scenario(scenario)
                .orderIds(
                        createdOrders.stream()
                                .map(Order::getId)
                                .toList()
                )
                .orderNos(
                        createdOrders.stream()
                                .map(Order::getOrderNo)
                                .toList()
                )
                .build();
    }

    /**
     * Mock 주문 한 건 생성
     */
    private Order createOneOrder(
            Store store,
            List<Menu> menus,
            List<PlatformSetting> platformSettings,
            MockOrderScenario requestedScenario
    ) {
        // MIXED라면 주문마다 실제 시나리오를 다시 선택
        MockOrderScenario actualScenario =
                resolveActualScenario(requestedScenario);

        // 플랫폼 4개 중 하나를 랜덤 선택
        PlatformSetting platformSetting =
                randomElement(platformSettings);

        // 시나리오에 맞게 메뉴와 수량 선택
        List<MenuQuantity> selectedMenus =
                selectMenus(menus, actualScenario);

        // 주문 당시 메뉴 스냅샷 생성
        List<OrderItem> orderItems =
                selectedMenus.stream()
                        .map(this::createOrderItem)
                        .toList();

        /*
         * 각 상세 항목의 계산 결과를 합산해서
         * orders에 저장할 전체 값을 만든다.
         */
        int totalAmount = orderItems.stream()
                .mapToInt(OrderItem::getItemMenuAmount)
                .sum();

        int totalMenuCost = orderItems.stream()
                .mapToInt(OrderItem::getItemMenuCost)
                .sum();

        int totalPackagingFee = orderItems.stream()
                .mapToInt(OrderItem::getItemPackagingAmount)
                .sum();

        int totalCookingTime = orderItems.stream()
                .mapToInt(OrderItem::getItemCookingTime)
                .sum();

        int commissionAmount = calculateCommission(
                totalAmount,
                platformSetting.getCommissionRate()
        );

        int couponCost =
                zeroIfNull(platformSetting.getCouponCost());

        int deliveryFee =
                zeroIfNull(platformSetting.getDeliveryFee());

        int platformSupportAmount =
                zeroIfNull(
                        platformSetting.getPlatformSupportAmount()
                );

        int netProfit =
                totalAmount
                        - commissionAmount
                        - couponCost
                        - deliveryFee
                        - totalMenuCost
                        - totalPackagingFee
                        + platformSupportAmount;

        /*
         * 일반 Mock 주문은 항상 WAITING으로 생성한다.
         * 조리중 전환 시점부터 cookingStartedAt이 기록되고
         * 지연 위험 계산이 시작된다.
         */
        Order order = Order.builder()
                .storeId(store.getId())
                .orderNo(generateOrderNo())
                .platformType(
                        platformSetting.getPlatformType()
                )
                .orderStatus(OrderStatus.WAITING)
                .totalAmount(totalAmount)
                .commissionAmount(commissionAmount)
                .couponCost(couponCost)
                .deliveryFee(deliveryFee)
                .platformSupportAmount(
                        platformSupportAmount
                )
                .totalMenuCost(totalMenuCost)
                .totalPackagingFee(totalPackagingFee)
                .netProfit(netProfit)
                .totalCookingTime(totalCookingTime)
                .orderedAt(java.time.LocalDateTime.now())
                .cookingStartedAt(null)
                .completedAt(null)
                .canceledAt(null)
                .refundedAt(null)
                .build();

        // 1. orders 저장
        int orderResult = orderMapper.save(order);

        if (orderResult != 1) {
            throw new RuntimeException(
                    "Mock 주문 저장 중 문제가 발생했습니다."
            );
        }

        /*
         * useGeneratedKeys로 획득한 orders.id를
         * 각 OrderItem의 외래키로 입력한다.
         */
        orderItems.forEach(item ->
                item.setOrderId(order.getId())
        );

        // 2. order_items 일괄 저장
        int itemResult =
                orderItemMapper.saveAll(orderItems);

        if (itemResult != orderItems.size()) {
            throw new RuntimeException(
                    "Mock 주문 상세 저장 중 문제가 발생했습니다."
            );
        }

        return order;
    }

    /**
     * 주문 상세 스냅샷과 항목별 계산값 생성
     */
    private OrderItem createOrderItem(
            MenuQuantity menuQuantity
    ) {
        Menu menu = menuQuantity.menu();
        int quantity = menuQuantity.quantity();

        int batchCapacity =
                Math.max(1, menu.getBatchCapacity());

        // 정수 나눗셈을 이용한 올림 계산
        int cookingCount =
                (quantity + batchCapacity - 1)
                        / batchCapacity;

        int itemCookingTime =
                cookingCount
                        * menu.getExpectedCookingTime();

        return OrderItem.builder()
                .menuId(menu.getId())
                .quantity(quantity)

                // 주문 당시 메뉴 정보 스냅샷
                .orderedMenuName(menu.getMenuName())
                .orderedMenuPrice(menu.getMenuPrice())
                .orderedMenuCost(menu.getMenuCost())
                .orderedPackagingFee(
                        menu.getPackagingFee()
                )
                .orderedCookingTime(
                        menu.getExpectedCookingTime()
                )
                .orderedBatchCapacity(batchCapacity)

                // 수량을 반영한 항목별 합계
                .itemMenuAmount(
                        menu.getMenuPrice() * quantity
                )
                .itemMenuCost(
                        menu.getMenuCost() * quantity
                )
                .itemPackagingAmount(
                        menu.getPackagingFee() * quantity
                )
                .itemCookingTime(itemCookingTime)
                .build();
    }

    /**
     * Mock 시나리오에 맞게 메뉴와 수량 선택
     */
    private List<MenuQuantity> selectMenus(
            List<Menu> menus,
            MockOrderScenario scenario
    ) {
        return switch (scenario) {
            case NORMAL ->
                    selectNormalMenus(menus);

            case GROUP ->
                    selectGroupMenus(menus);

            case PREMIUM ->
                    selectPremiumMenus(menus);

            case DELAY_TEST ->
                    selectDelayTestMenus(menus);

            case MIXED ->
                // createOneOrder에서 실제 시나리오로 변환하므로 예비 처리
                    selectNormalMenus(menus);

            case ALLERGY ->
                    selectAllergyMenus(menus);

            case LOSS ->
                    selectLossMenus(menus);
        };
    }

    /**
     * 일반 주문
     * 메뉴 1~2종, 메뉴당 1~3개
     */
    private List<MenuQuantity> selectNormalMenus(
            List<Menu> menus
    ) {
        int menuCount = Math.min(
                menus.size(),
                randomInt(1, 2)
        );

        List<Menu> selectedMenus =
                selectDistinctMenus(menus, menuCount);

        return selectedMenus.stream()
                .map(menu ->
                        new MenuQuantity(
                                menu,
                                randomInt(1, 3)
                        )
                )
                .toList();
    }

    /**
     * 단체 주문
     * 최소 한 메뉴를 6개 이상 주문
     */
    private List<MenuQuantity> selectGroupMenus(
            List<Menu> menus
    ) {
        List<MenuQuantity> result =
                new ArrayList<>();

        Menu mainMenu = randomElement(menus);

        result.add(
                new MenuQuantity(
                        mainMenu,
                        randomInt(6, 12)
                )
        );

        // 일정 확률로 다른 메뉴도 함께 주문
        if (menus.size() > 1 && randomInt(0, 1) == 1) {
            Menu additionalMenu = menus.stream()
                    .filter(menu ->
                            !menu.getId().equals(
                                    mainMenu.getId()
                            )
                    )
                    .findAny()
                    .orElse(mainMenu);

            result.add(
                    new MenuQuantity(
                            additionalMenu,
                            randomInt(2, 5)
                    )
            );
        }

        return result;
    }

    /**
     * 프리미엄 주문
     * 판매가가 높거나 조리시간이 긴 메뉴 선택
     */
    private List<MenuQuantity> selectPremiumMenus(
            List<Menu> menus
    ) {
        Menu premiumMenu = menus.stream()
                .max(
                        Comparator
                                .comparingInt(Menu::getMenuPrice)
                                .thenComparingInt(
                                        Menu::getExpectedCookingTime
                                )
                )
                .orElseThrow();

        List<MenuQuantity> result =
                new ArrayList<>();

        result.add(
                new MenuQuantity(
                        premiumMenu,
                        randomInt(1, 3)
                )
        );

        if (menus.size() > 1 && randomInt(0, 1) == 1) {
            Menu additionalMenu = menus.stream()
                    .filter(menu ->
                            !menu.getId().equals(
                                    premiumMenu.getId()
                            )
                    )
                    .findAny()
                    .orElse(premiumMenu);

            result.add(
                    new MenuQuantity(
                            additionalMenu,
                            randomInt(1, 2)
                    )
            );
        }

        return result;
    }

    /**
     * 지연 테스트 주문
     *
     * 생성 상태는 WAITING이지만,
     * 조리시간이 긴 메뉴와 많은 수량을 선택한다.
     * 이후 COOKING으로 변경하면 지연 테스트에 사용 가능하다.
     */
    private List<MenuQuantity> selectDelayTestMenus(
            List<Menu> menus
    ) {
        Menu slowMenu = menus.stream()
                .max(
                        Comparator.comparingInt(
                                Menu::getExpectedCookingTime
                        )
                )
                .orElseThrow();

        return List.of(
                new MenuQuantity(
                        slowMenu,
                        randomInt(5, 10)
                )
        );
    }

    /**
     * MIXED 요청을 실제 주문별 시나리오로 변환
     */
    private MockOrderScenario resolveActualScenario(
            MockOrderScenario requestedScenario
    ) {
        if (requestedScenario != MockOrderScenario.MIXED) {
            return requestedScenario;
        }

        int randomValue = randomInt(1, 100);

        if (randomValue <= 40) {
            return MockOrderScenario.NORMAL;
        }

        if (randomValue <= 60) {
            return MockOrderScenario.GROUP;
        }

        if (randomValue <= 75) {
            return MockOrderScenario.PREMIUM;
        }

        if (randomValue <= 85) {
            return MockOrderScenario.ALLERGY;
        }

        if (randomValue <= 95) {
            return MockOrderScenario.LOSS;
        }

        return MockOrderScenario.DELAY_TEST;
    }

    /**
     * 알러지 주문
     */
    private List<MenuQuantity> selectAllergyMenus(
            List<Menu> menus
    ) {
        Menu menu = randomElement(menus);

        return List.of(
                new MenuQuantity(menu, 1)
        );
    }

    /**
     * 손실 위험 주문
     */
    private List<MenuQuantity> selectLossMenus(
            List<Menu> menus
    ) {
        Menu expensiveMenu = menus.stream()
                .max(Comparator.comparingInt(Menu::getMenuPrice))
                .orElseThrow();

        return List.of(
                new MenuQuantity(expensiveMenu, 8)
        );
    }

    /**
     * 중개이용료 계산
     * 주문금액 × 수수료율 ÷ 100
     */
    private int calculateCommission(
            int totalAmount,
            BigDecimal commissionRate
    ) {
        if (commissionRate == null) {
            return 0;
        }

        // 주문금액 × 수수료율 ÷ 100
        BigDecimal rawCommission =
                BigDecimal.valueOf(totalAmount)
                        .multiply(commissionRate)
                        .divide(BigDecimal.valueOf(100));

        // 10원 단위로 올림
        return rawCommission
                .divide(
                        BigDecimal.TEN,
                        0,
                        RoundingMode.CEILING
                )
                .multiply(BigDecimal.TEN)
                .intValue();
    }

    /**
     * 주문번호 생성
     * 예: ORD-20260616-A1B2C3D4
     */
    private String generateOrderNo() {
        String date =
                LocalDate.now().format(
                        DateTimeFormatter.BASIC_ISO_DATE
                );

        String randomText =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        return "ORD-" + date + "-" + randomText;
    }

    /**
     * 목록에서 중복 없이 메뉴 선택
     */
    private List<Menu> selectDistinctMenus(
            List<Menu> menus,
            int count
    ) {
        List<Menu> copiedMenus =
                new ArrayList<>(menus);

        Collections.shuffle(copiedMenus);

        return copiedMenus.subList(
                0,
                Math.min(count, copiedMenus.size())
        );
    }

    private <T> T randomElement(List<T> values) {
        return values.get(
                randomInt(0, values.size() - 1)
        );
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom
                .current()
                .nextInt(min, max + 1);
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * Mock 생성 과정에서만 사용하는 내부 묶음 객체
     */
    private record MenuQuantity(
            Menu menu,
            int quantity
    ) {
    }

    @Transactional(rollbackFor = Exception.class)
    public MockOrderCreateResponse createPeak(Long userId) {

        create(
                userId,
                new MockOrderCreateRequest(
                        2,
                        MockOrderScenario.GROUP
                )
        );

        create(
                userId,
                new MockOrderCreateRequest(
                        1,
                        MockOrderScenario.PREMIUM
                )
        );

        create(
                userId,
                new MockOrderCreateRequest(
                        1,
                        MockOrderScenario.ALLERGY
                )
        );

        create(
                userId,
                new MockOrderCreateRequest(
                        1,
                        MockOrderScenario.LOSS
                )
        );

        return create(
                userId,
                new MockOrderCreateRequest(
                        1,
                        MockOrderScenario.DELAY_TEST
                )
        );
    }
    /**
     * 로그인 사용자의 매장에 속한 Mock 주문 전체 삭제
     */
    @Transactional(rollbackFor = Exception.class)
    public MockOrderDeleteResponse deleteAll(Long userId) {
        // 1. 로그인 회원의 활성 매장 조회
        Store store = storeMapper.findByUserId(userId);

        if (store == null) {
            throw new BadRequestException(
                    "Mock 주문을 삭제할 활성 매장이 없습니다."
            );
        }

        // 2. 해당 매장에 속한 주문 전체 삭제
        int deletedOrderCount =
                orderMapper.deleteAllByStoreId(
                        store.getId()
                );

        // 3. 삭제된 주문 개수 반환
        return MockOrderDeleteResponse.builder()
                .deletedOrderCount(deletedOrderCount)
                .build();
    }
}

