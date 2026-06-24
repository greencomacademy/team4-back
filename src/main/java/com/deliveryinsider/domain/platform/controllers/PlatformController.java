package com.deliveryinsider.domain.platform.controllers;

import com.deliveryinsider.global.enums.PlatformType;
import com.deliveryinsider.domain.platform.requests.PlatformUpdateRequest;
import com.deliveryinsider.domain.platform.responses.PlatformResponse;
import com.deliveryinsider.domain.platform.services.PlatformService;
import com.deliveryinsider.global.responses.GlobalRes;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/platform-settings")
public class PlatformController {

    private final PlatformService platformService;

    /**
     * 내 활성 매장의 플랫폼 설정 전체 조회
     * GET /api/platform-settings
     */
    @GetMapping
    public ResponseEntity<GlobalRes<List<PlatformResponse>>> findAll(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<PlatformResponse> result =
                platformService.findAll(userId);

        return ResponseEntity.ok(
                GlobalRes.<List<PlatformResponse>>builder()
                        .code("00")
                        .message("플랫폼 설정 조회 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * 특정 플랫폼 정산 조건 부분 수정
     * PATCH /api/platform-settings/{platformType}
     */
    @PatchMapping("/{platformType}")
    public ResponseEntity<GlobalRes<PlatformResponse>> update(
            Authentication authentication,
            @PathVariable PlatformType platformType,
            @Valid @RequestBody PlatformUpdateRequest updateReq
    ) {
        Long userId = extractUserId(authentication);

        PlatformResponse result =
                platformService.update(
                        userId,
                        platformType,
                        updateReq
                );

        return ResponseEntity.ok(
                GlobalRes.<PlatformResponse>builder()
                        .code("00")
                        .message("플랫폼 설정 수정 성공")
                        .data(result)
                        .build()
        );
    }

    /**
     * JWT subject에서 로그인 회원 PK 추출
     */
    private Long extractUserId(Authentication authentication) {
        Claims claims =
                (Claims) authentication.getPrincipal();

        return Long.valueOf(claims.getSubject());
    }
}