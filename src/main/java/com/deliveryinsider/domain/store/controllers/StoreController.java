package com.deliveryinsider.domain.store.controllers;

import com.deliveryinsider.domain.store.requests.StoreCreateReq;
import com.deliveryinsider.domain.store.requests.StoreUpdateReq;
import com.deliveryinsider.domain.store.responses.StoreRes;
import com.deliveryinsider.domain.store.services.StoreService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    /**
     * 매장 등록
     * POST /api/stores
     */
    @PostMapping("/newstore")
    public ResponseEntity<GlobalRes<StoreRes>> create(
            Authentication authentication,
            @Valid @RequestBody StoreCreateReq storeCreateReq
    ) {
        Long userId = extractUserId(authentication);

        StoreRes result =
                storeService.create(userId, storeCreateReq);

        return ResponseEntity.ok(
                GlobalRes.<StoreRes>builder()
                        .code("00")
                        .message("매장 등록 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 내 활성 매장 조회
     * GET /api/stores/me
     */
    @GetMapping("/me")
    public ResponseEntity<GlobalRes<StoreRes>> findMyStore(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        StoreRes result =
                storeService.findMyStore(userId);

        return ResponseEntity.ok(
                GlobalRes.<StoreRes>builder()
                        .code("00")
                        .message("내 매장 조회 성공")
                        .data(result)
                        .build()
        );
    }

    /*
    * 내 매장정보 수정 코드
    *
    * */
    @PatchMapping("/me")
    public ResponseEntity<GlobalRes<StoreRes>> updateMyStore(
            Authentication authentication,
            @Valid @RequestBody StoreUpdateReq storeUpdateReq
    ){
        Long userId = extractUserId(authentication);

        StoreRes result =
                storeService.update(userId, storeUpdateReq);

        return ResponseEntity.ok(
                GlobalRes.<StoreRes>builder()
                        .code("00")
                        .message("매장 수정 성공")
                        .data(result)
                        .build()
        );
    }

    // 내 매장정보 삭제
    @DeleteMapping("/me")
    public ResponseEntity<GlobalRes<Void>> deleteMyStore(
            Authentication authentication
    ){
        Long userId = extractUserId(authentication);

        storeService.delete(userId);
        return ResponseEntity.ok(
                GlobalRes.<Void>builder()
                        .code("00")
                        .message("매장 삭제 성공")
                        .build()
        );
    }




    /**
     * JWT Claims의 subject에서 로그인 회원 PK 추출
     */
    private Long extractUserId(Authentication authentication) {
        Claims claims =
                (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}