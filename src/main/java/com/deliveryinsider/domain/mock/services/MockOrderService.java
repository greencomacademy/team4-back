package com.deliveryinsider.domain.mock.services;

import com.deliveryinsider.domain.menus.entities.Menu;
import com.deliveryinsider.domain.menus.mapper.MenuMapper;
import com.deliveryinsider.domain.mock.requests.MockOrderCreateRequest;
import com.deliveryinsider.domain.mock.responses.MockOrderCreateResponse;
import com.deliveryinsider.domain.mock.responses.MockOrderDeleteResponse;
import com.deliveryinsider.domain.order.entities.Order;
import com.deliveryinsider.domain.order.entities.OrderItem;
import com.deliveryinsider.domain.order.entities.OrderRequest;
import com.deliveryinsider.domain.order.enums.MockOrderScenario;
import com.deliveryinsider.domain.order.mapper.OrderItemMapper;
import com.deliveryinsider.domain.order.mapper.OrderMapper;
import com.deliveryinsider.domain.order.mapper.OrderRequestMapper;
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
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MockOrderService {

    private final StoreMapper storeMapper;
    private final MenuMapper menuMapper;
    private final PlatformMapper platformSettingMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderRequestMapper orderRequestMapper;

    /**
     * Mock 주문 여러 건 생성
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
        // 4. 요청받은 시나리오에 맞게 실제 생성할 시나리오 목록 생성
        List<MockOrderScenario> creationScenarios =
            createScenarioPlan(
                scenario,
                requestedCount
            );

        List<Order> createdOrders = new ArrayList<>();

        for (MockOrderScenario creationScenario : creationScenarios) {
            Order createdOrder = createOneOrder(
                store,
                menus,
                platformSettings,
                creationScenario
            );

            createdOrders.add(createdOrder);
        }

        // 5. 생성 결과 응답
        return MockOrderCreateResponse.builder()
                .requestedCount(creationScenarios.size())
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
     * 요청받은 Mock 시나리오를 실제 생성할 시나리오 목록으로 변환한다.
     * PEAK_SET은 발표용 세트이므로
     * NORMAL / REQUEST_RISK / ALLERGY / DELAY_TEST / LOSS를
     * 각각 1건씩 생성한다.
     */
    private List<MockOrderScenario> createScenarioPlan(
        MockOrderScenario scenario,
        int requestedCount
    ) {
        if (scenario == MockOrderScenario.PEAK_SET) {
            return List.of(
                MockOrderScenario.NORMAL,
                MockOrderScenario.REQUEST_RISK,
                MockOrderScenario.ALLERGY,
                MockOrderScenario.DELAY_TEST,
                MockOrderScenario.LOSS
            );
        }

        List<MockOrderScenario> scenarios = new ArrayList<>();

        for (int i = 0; i < requestedCount; i++) {
            scenarios.add(scenario);
        }

        return scenarios;
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
        // 요청 시나리오를 실제 생성 시나리오로 변환한다.
        // MIXED는 하나의 주문 안에 요청사항을 2개 이상 합치는 전용 시나리오다.
        MockOrderScenario actualScenario =
                resolveActualScenario(requestedScenario);

        // 플랫폼 4개 중 하나를 랜덤 선택
        PlatformSetting platformSetting =
                randomElement(platformSettings);

        // 시나리오에 맞게 메뉴와 수량 선택
        List<MenuQuantity> selectedMenus =
                selectMenus(menus, actualScenario);

        // 매장 최소주문금액 이상이 되도록 Mock 주문 수량 보정
        selectedMenus = ensureMinimumOrderAmount(
                selectedMenus,
                zeroIfNull(store.getMinimumOrderAmount())
        );

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
         * LOSS 시나리오는 프론트/대시보드에서 손실 위험을 확인하기 위한 주문이다.
         * 실제 메뉴·플랫폼 조합만으로 손실이 나지 않을 수도 있으므로,
         * Mock 주문에서는 쿠폰 부담금을 추가해 netProfit이 0 이하가 되도록 보정한다.
         */
        if (actualScenario == MockOrderScenario.LOSS
            && netProfit > 0) {

            int additionalCouponCost =
                netProfit + randomInt(500, 2000);

            couponCost += additionalCouponCost;

            netProfit =
                totalAmount
                    - commissionAmount
                    - couponCost
                    - deliveryFee
                    - totalMenuCost
                    - totalPackagingFee
                    + platformSupportAmount;
        }

        /*
         * 일반 Mock 주문은 항상 WAITING으로 생성한다.
         * 조리중 전환 시점부터 cookingStartedAt이 기록되고
         * 지연 위험 계산이 시작된다.
         */
        Order order = Order.builder()
                .storeId(store.getId())
                .orderNo(generateOrderNo())
                .platformOrderNumber(
                generatePlatformOrderNumber(
                    platformSetting.getPlatformType()
                     )
                )
                .deliveryAddress(generateDeliveryAddress())
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
    // 3. 주문 요구사항 저장
    OrderRequest orderRequest =
        createOrderRequest(actualScenario, order.getId());

    int requestResult =
        orderRequestMapper.save(orderRequest);

    if (requestResult != 1) {
            throw new RuntimeException(
                "Mock 주문 요구사항 저장 중 문제가 발생했습니다."
            );
        }

        return order;
    }
    /**
     * Mock 시나리오에 맞는 고객 요구사항 생성
     */
    private OrderRequest createOrderRequest(
        MockOrderScenario scenario,
        Long orderId
    ) {
        return switch (scenario) {
            case NORMAL, PEAK_SET -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("수저는 1개만 넣어주세요.")
                .riskType("NORMAL")
                .riskLevel("SAFE")
                .detectedKeywords("")
                .analysisMessage("일반 요청사항입니다.")
                .build();

            case MIXED -> createMixedOrderRequest(orderId);

            case REQUEST_RISK -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("늦으면 취소할게요. 서비스 많이 주세요.")
                .riskType("DISPUTE")
                .riskLevel("WARNING")
                .detectedKeywords("늦으면,취소,서비스 많이")
                .analysisMessage("분쟁 가능성과 과도 요청이 포함된 주문입니다. 접수 전 제공 가능 범위를 확인해 주세요.")
                .build();

            case ALLERGY -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("땅콩 알러지가 있습니다. 견과류는 절대 빼주세요.")
                .riskType("ALLERGY")
                .riskLevel("DANGER")
                .detectedKeywords("땅콩,알러지,견과류")
                .analysisMessage("알러지 관련 요청입니다. 조리 전 재료와 제외 요청을 반드시 확인해 주세요.")
                .build();

            case GROUP -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("회사 회의용입니다. 시간 맞춰서 부탁드립니다.")
                .riskType("DELIVERY")
                .riskLevel("CAUTION")
                .detectedKeywords("시간,회의")
                .analysisMessage("단체 주문입니다. 조리시간과 포장 수량을 확인해 주세요.")
                .build();

            case PREMIUM -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("소스는 따로 담아주시고 포장 꼼꼼히 부탁드립니다.")
                .riskType("DELIVERY")
                .riskLevel("SAFE")
                .detectedKeywords("소스,포장")
                .analysisMessage("포장 요청사항을 확인해 주세요.")
                .build();

            case DELAY_TEST -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("회사 회의용입니다. 시간 맞춰서 부탁드립니다.")
                .riskType("DELIVERY")
                .riskLevel("CAUTION")
                .detectedKeywords("시간,회의")
                .analysisMessage("지연 테스트 주문입니다. 조리중으로 변경한 뒤 지연 위험 계산을 확인해 주세요.")
                .build();

            case LOSS -> OrderRequest.builder()
                .orderId(orderId)
                .requestText("리뷰 쓸테니 소스랑 사이드 서비스 많이 주세요.")
                .riskType("EXCESSIVE")
                .riskLevel("CAUTION")
                .detectedKeywords("리뷰,서비스 많이,소스,사이드")
                .analysisMessage("과도 요청과 손실 위험이 있는 주문입니다. 제공 기준과 예상 순수익을 확인해 주세요.")
                .build();
        };
    }

    /**
     * 랜덤 혼합 요청사항 생성
     * 일반 주문으로 빠지지 않고, 요청사항 이슈 2개 이상을 한 주문에 합친다.
     */
    private OrderRequest createMixedOrderRequest(Long orderId) {
        List<RequestIssue> issues = new ArrayList<>(List.of(
            new RequestIssue(
                "ALLERGY",
                "DANGER",
                "알러지",
                "땅콩 알러지가 있습니다. 견과류는 절대 빼주세요.",
                "땅콩,알러지,견과류",
                1
            ),
            new RequestIssue(
                "DISPUTE",
                "WARNING",
                "분쟁 가능",
                "늦으면 취소할게요. 시간 맞춰서 보내주세요.",
                "늦으면,취소,시간",
                2
            ),
            new RequestIssue(
                "EXCESSIVE",
                "WARNING",
                "과도 요청",
                "리뷰 쓸테니 소스랑 사이드 서비스 많이 주세요.",
                "리뷰,서비스 많이,소스,사이드",
                3
            ),
            new RequestIssue(
                "GROUP",
                "CAUTION",
                "배달사항 확인",
                "회사 회의용입니다. 시간 맞춰서 부탁드립니다.",
                "회사,회의,시간",
                4
            ),
            new RequestIssue(
                "REQUEST_RISK",
                "WARNING",
                "요청사항 확인",
                "수저는 넉넉히 넣어주시고 요청사항 꼭 확인해 주세요.",
                "수저,요청사항,확인",
                5
            )
        ));

        Collections.shuffle(issues);

        int issueCount = randomInt(2, 3);
        List<RequestIssue> selectedIssues = issues.subList(0, issueCount);

        RequestIssue primaryIssue = selectedIssues.stream()
            .min(Comparator.comparingInt(RequestIssue::priority))
            .orElse(selectedIssues.get(0));

        String requestText = String.join(
            " ",
            selectedIssues.stream()
                .map(RequestIssue::requestText)
                .toList()
        );

        String detectedKeywords = String.join(
            ",",
            selectedIssues.stream()
                .map(RequestIssue::detectedKeywords)
                .toList()
        );

        String issueSummary = String.join(
            " + ",
            selectedIssues.stream()
                .map(RequestIssue::label)
                .toList()
        );

        String riskLevel = selectedIssues.stream()
            .anyMatch(issue -> "DANGER".equals(issue.riskLevel()))
                ? "DANGER"
                : "WARNING";

        return OrderRequest.builder()
            .orderId(orderId)
            .requestText(requestText)
            .riskType(primaryIssue.riskType())
            .riskLevel(riskLevel)
            .detectedKeywords(detectedKeywords)
            .analysisMessage(
                "여러 요청사항(" + issueSummary + ")이 함께 포함된 주문입니다. 접수 전 반드시 확인해 주세요."
            )
            .build();
    }

    /**
     * Mock 플랫폼 주문번호 생성
     * 예: B-MOCK-20260624-A1B2C3D4
     */
    private String generatePlatformOrderNumber(
        com.deliveryinsider.global.enums.PlatformType platformType
    ) {
        String prefix = switch (platformType) {
            case BAEMIN -> "B";
            case COUPANG_EATS -> "C";
            case YOGIYO -> "Y";
            case DDANGYO -> "D";
        };

        String date =
            LocalDate.now().format(
                DateTimeFormatter.BASIC_ISO_DATE
            );

        String randomText =
            UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-MOCK-" + date + "-" + randomText;
    }

    /**
     * Mock 배달주소 생성
     */
    private String generateDeliveryAddress() {
        List<String> addresses = List.of(
            "대구광역시 동구 동대구로 101",
            "대구광역시 수성구 달구벌대로 2400",
            "대구광역시 중구 중앙대로 88",
            "대구광역시 북구 침산로 33",
            "대구광역시 남구 중앙대로 210"
        );

        return randomElement(addresses);
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
     * Mock 주문 총액이 매장의 최소주문금액 이상이 되도록 수량을 보정한다.
     * 발표용 Mock 데이터가 실제 배달 주문 조건과 맞지 않는 문제를 방지하기 위한 처리다.
     */
    private List<MenuQuantity> ensureMinimumOrderAmount(
            List<MenuQuantity> selectedMenus,
            int minimumOrderAmount
    ) {
        if (minimumOrderAmount <= 0 || selectedMenus.isEmpty()) {
            return selectedMenus;
        }

        int currentAmount = selectedMenus.stream()
                .mapToInt(menuQuantity ->
                        menuQuantity.menu().getMenuPrice()
                                * menuQuantity.quantity()
                )
                .sum();

        if (currentAmount >= minimumOrderAmount) {
            return selectedMenus;
        }

        int targetIndex = 0;

        for (int i = 0; i < selectedMenus.size(); i++) {
            if (selectedMenus.get(i).menu().getMenuPrice() > 0) {
                targetIndex = i;
                break;
            }
        }

        MenuQuantity target = selectedMenus.get(targetIndex);
        int menuPrice = target.menu().getMenuPrice();

        if (menuPrice <= 0) {
            return selectedMenus;
        }

        int lackAmount = minimumOrderAmount - currentAmount;
        int additionalQuantity = (int) Math.ceil(
                (double) lackAmount / menuPrice
        );

        List<MenuQuantity> adjustedMenus = new ArrayList<>(selectedMenus);
        adjustedMenus.set(
                targetIndex,
                new MenuQuantity(
                        target.menu(),
                        target.quantity() + additionalQuantity
                )
        );

        return adjustedMenus;
    }

    /**
     * Mock 시나리오에 맞게 메뉴와 수량 선택
     */
    private List<MenuQuantity> selectMenus(
        List<Menu> menus,
        MockOrderScenario scenario
    ) {
        return switch (scenario) {
            case NORMAL,
                 REQUEST_RISK,
                 ALLERGY ->
                selectNormalMenus(menus);

            case GROUP ->
                selectGroupMenus(menus);

            case PREMIUM ->
                selectPremiumMenus(menus);

            case DELAY_TEST ->
                selectDelayTestMenus(menus);

            case LOSS ->
                selectLossMenus(menus);

            case MIXED ->
                selectMixedMenus(menus);

            case PEAK_SET ->
                // createScenarioPlan에서 실제 시나리오로 변환되므로 예비 처리
                selectNormalMenus(menus);
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
     * 랜덤 혼합 주문
     * 요청사항이 2개 이상 섞인 상황을 보여주기 위해 메뉴도 2종 이상 선택한다.
     */
    private List<MenuQuantity> selectMixedMenus(
            List<Menu> menus
    ) {
        List<MenuQuantity> result = new ArrayList<>(selectNormalMenus(menus));

        if (menus.size() <= 1 || result.size() >= 2) {
            return result;
        }

        Long selectedMenuId = result.get(0).menu().getId();

        Menu additionalMenu = menus.stream()
            .filter(menu -> !menu.getId().equals(selectedMenuId))
            .findAny()
            .orElse(result.get(0).menu());

        result.add(
            new MenuQuantity(
                additionalMenu,
                randomInt(1, 2)
            )
        );

        return result;
    }
   
    /**
     * 손실 위험 주문
     * 원가나 포장비가 높은 메뉴를 우선 선택한다.
     * 이후 createOneOrder에서 쿠폰 부담금을 보정해
     * netProfit이 0 이하가 되도록 만든다.
     */
    private List<MenuQuantity> selectLossMenus(
        List<Menu> menus
    ) {
        Menu lossMenu = menus.stream()
            .max(
                Comparator.<Menu>comparingInt(
                    menu ->
                        menu.getMenuCost()
                            + menu.getPackagingFee()
                ).thenComparingInt(
                    Menu::getExpectedCookingTime
                )
            )
            .orElseThrow();

        return List.of(
            new MenuQuantity(
                lossMenu,
                randomInt(1, 3)
            )
        );
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
     * 요청받은 시나리오를 실제 생성 시나리오로 변환한다.
     * MIXED는 일반 주문 중 랜덤 선택이 아니라,
     * 여러 요청사항을 한 주문에 합치는 전용 시나리오로 유지한다.
     */
    private MockOrderScenario resolveActualScenario(
        MockOrderScenario requestedScenario
    ) {
        return requestedScenario;
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
     * 예: ORD-MOCK-20260616-A1B2C3D4
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

        return "ORD-MOCK-" + date + "-" + randomText;
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

    /**
     * 랜덤 혼합 요청사항 생성을 위한 내부 객체
     */
    private record RequestIssue(
            String riskType,
            String riskLevel,
            String label,
            String requestText,
            String detectedKeywords,
            int priority
    ) {
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
                orderMapper.deleteMockOrdersStoreId(
                        store.getId()
                );

        // 3. 삭제된 주문 개수 반환
        return MockOrderDeleteResponse.builder()
                .deletedOrderCount(deletedOrderCount)
                .build();
    }
}

