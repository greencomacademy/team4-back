package com.deliveryinsider.domain.menus.controllers;


import com.deliveryinsider.domain.menus.requests.MenuCreateRequest;
import com.deliveryinsider.domain.menus.requests.MenuUpdateRequest;
import com.deliveryinsider.domain.menus.responses.MenuMarginAnalysisResponse;
import com.deliveryinsider.domain.menus.responses.MenuResponse;
import com.deliveryinsider.domain.menus.services.MenuService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    /**
     * 메뉴 등록
     * POST /api/menus
     */
    @PostMapping
    public ResponseEntity<GlobalRes<MenuResponse>> create(
            Authentication authentication,
            @Valid @RequestBody MenuCreateRequest createReq
    ) {
        Long userId = extractUserId(authentication);

        MenuResponse result = menuService.create(
                userId,
                createReq
        );

        return ResponseEntity.ok(
                GlobalRes.<MenuResponse>builder()
                        .code("00")
                        .message("메뉴 등록 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 내 활성 매장의 메뉴 전체 조회
     * GET /api/menus
     */
    @GetMapping
    public ResponseEntity<GlobalRes<List<MenuResponse>>> findAll(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<MenuResponse> result =
                menuService.findAll(userId);

        return ResponseEntity.ok(
                GlobalRes.<List<MenuResponse>>builder()
                        .code("00")
                        .message("메뉴 목록 조회 성공")
                        .data(result)
                        .build()
        );
    }
    /**
     * 내 활성 매장의 메뉴별 마진 및 조리 부담 분석
     * GET /api/menus/margin-analysis
     */
    @GetMapping("/margin-analysis")
    public ResponseEntity<
            GlobalRes<List<MenuMarginAnalysisResponse>>
            > findMarginAnalysis(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<MenuMarginAnalysisResponse> result =
                menuService.findMarginAnalysis(userId);

        return ResponseEntity.ok(
                GlobalRes
                        .<List<MenuMarginAnalysisResponse>>builder()
                        .code("00")
                        .message("메뉴별 마진 분석 조회 성공")
                        .data(result)
                        .build()
        );
    }



    /**
     * 메뉴 상세 조회
     * GET /api/menus/{menuId}
     */
    @GetMapping("/{menuId}")
    public ResponseEntity<GlobalRes<MenuResponse>> findOne(
            Authentication authentication,
            @PathVariable Long menuId
    ) {
        Long userId = extractUserId(authentication);

        MenuResponse result = menuService.findOne(
                userId,
                menuId
        );

        return ResponseEntity.ok(
                GlobalRes.<MenuResponse>builder()
                        .code("00")
                        .message("메뉴 상세 조회 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 메뉴 부분 수정
     * PATCH /api/menus/{menuId}
     */
    @PatchMapping("/{menuId}")
    public ResponseEntity<GlobalRes<MenuResponse>> update(
            Authentication authentication,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest updateReq
    ) {
        Long userId = extractUserId(authentication);

        MenuResponse result = menuService.update(
                userId,
                menuId,
                updateReq
        );

        return ResponseEntity.ok(
                GlobalRes.<MenuResponse>builder()
                        .code("00")
                        .message("메뉴 수정 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 메뉴 소프트 삭제
     * DELETE /api/menus/{menuId}
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<GlobalRes<Void>> delete(
            Authentication authentication,
            @PathVariable Long menuId
    ) {
        Long userId = extractUserId(authentication);

        menuService.delete(userId, menuId);

        return ResponseEntity.ok(
                GlobalRes.<Void>builder()
                        .code("00")
                        .message("메뉴 삭제 성공")
                        .build()
        );
    }

    /**
     * JWT Claims의 subject에서 로그인 회원 PK 추출
     */
    private Long extractUserId(
            Authentication authentication
    ) {
        Claims claims =
                (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}