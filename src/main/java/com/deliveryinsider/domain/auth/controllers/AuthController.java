package com.deliveryinsider.domain.auth.controllers;

import com.deliveryinsider.domain.auth.responses.ReissueTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.deliveryinsider.domain.auth.requests.LoginRequest;
import com.deliveryinsider.domain.auth.requests.RegisterRequest;
import com.deliveryinsider.domain.auth.responses.LoginResponse;
import com.deliveryinsider.domain.auth.responses.RegisterResponse;
import com.deliveryinsider.domain.auth.services.AuthService;
import com.deliveryinsider.global.responses.GlobalRes;

import lombok.RequiredArgsConstructor;

/**
 * 인증(Auth) 관련 API를 처리하는 Controller
 * - 로그인
 * - 회원가입
 * - Access Token 재발급
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 로그인 API
     * - 이메일과 비밀번호를 확인한다.
     * - 로그인 성공 시 Access Token을 반환
     * - Refresh Token은 Cookie에 저장
     */
    @PostMapping("/login")
    public ResponseEntity<GlobalRes<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginReq, HttpServletResponse response
    ) {
        LoginResponse result = authService.login(loginReq, response);
        return ResponseEntity.ok(GlobalRes.<LoginResponse>builder()
                .code("00")
                .message("로그인 성공")
                .data(result)
                .build());
    }

    /**
     * Access Token 재발급 API
     * - Cookie의 Refresh Token을 확인
     * - 새로운 Access Token을 발급
     */
    @PostMapping("/reissue-token")
    public ResponseEntity<GlobalRes<ReissueTokenResponse>> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        ReissueTokenResponse result = authService.reissue(request, response);
        return ResponseEntity.ok(GlobalRes.<ReissueTokenResponse>builder()
                        .code("00")
                        .message("토큰 재발급 완료")
                        .data(result)
                        .build()
        );
    }

    /**
     * 회원가입 API
     * - 이메일 중복을 확인한다.
     * - 회원가입 진행
     */
    @PostMapping("/register")
    public ResponseEntity<GlobalRes<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        RegisterResponse result = authService.register(registerRequest);
        return ResponseEntity.ok(GlobalRes.<RegisterResponse>builder()
                .code("00")
                .message("회원가입 성공")
                .data(result).build());
    }

}
