package com.deliveryinsider.domain.auth.services;

import com.deliveryinsider.domain.auth.responses.ReissueTokenResponse;
import com.deliveryinsider.global.errors.custom.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.deliveryinsider.domain.auth.mapper.AuthMapper;
import com.deliveryinsider.domain.auth.requests.LoginRequest;
import com.deliveryinsider.domain.auth.requests.RegisterRequest;
import com.deliveryinsider.domain.auth.responses.LoginResponse;
import com.deliveryinsider.domain.auth.responses.RegisterResponse;
import com.deliveryinsider.domain.user.entities.User;
import com.deliveryinsider.domain.user.responses.UserResponse;
import com.deliveryinsider.global.errors.custom.DuplicatedRecordException;
import com.deliveryinsider.global.errors.custom.NotRegisteredException;
import com.deliveryinsider.global.security.cookie.CookieManager;
import com.deliveryinsider.global.security.jwt.JwtConfig;
import com.deliveryinsider.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieManager cookieManager;
    private final JwtConfig jwtConfig;

    /**
     *  로그인 처리
     *  - 이메일/비밀번호 확인
     *  - 로그인 성공 시 Access Token과 Refresh Token 을 발급
     *  - Refresh Token을 DB와 Cookie에 저장
     **/

    @Transactional
    public LoginResponse login(
            LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        // 이메일로 회원 조회
        User user = authMapper.findByEmail(loginRequest.email());

        // 회원이 없으면 로그인 실패
        if (user == null) {
            throw new NotRegisteredException(
                    "이메일 또는 비밀번호를 확인해 주세요."
            );
        }

        // 입력한 비밀번호와 DB의 암호화된 비밀번호 비교
        boolean passwordMatches = passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()
        );

        // 비밀번호가 일치하지 않으면 로그인 실패
        if (!passwordMatches) {
            throw new NotRegisteredException(
                    "이메일 또는 비밀번호를 확인해 주세요."
            );
        }

        // JWT의 Access Token 생성 (인증에 사용)
        String accessToken =
                jwtProvider.generateAccessToken(user);

        // JWT의 Refresh Token 생성 (토큰 재발급에 사용)
        String refreshToken =
                jwtProvider.generateRefreshToken(user);

        // DB에 Refresh Token 저장
        authMapper.updateRefreshToken(
                user.getId(),
                refreshToken
        );

        // Refresh Token을 쿠키에 저장
        cookieManager.setCookie(
                response,
                jwtConfig.refreshTokenCookieName(),
                refreshToken,
                jwtConfig.refreshTokenCookieExpiry(),
                jwtConfig.reissUri()
        );

        // Access Token 과 사용자 정보 반환
        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(
                        UserResponse.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .build()
                )
                .build();
    }

    /**
     * Access Token 재발급
     * - Cookie에 저장된 Refresh Token을 가져온다
     * - DB에 저장된 Refresh Token과 비교
     * - 토큰이 일치하면 새로운 Access Token 발급
     */

    @Transactional
    public ReissueTokenResponse reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Cookie 에서 Refresh Token 꺼내기
        String refreshToken = jwtProvider
                .extractRefreshToken(request)
                .orElseThrow(() ->
                        new InvalidTokenException("Refresh Token이 없습니다.")
                );
        // Refresh Token 안에 저장된 회원 번호(id) 추출
        Long userId = Long.valueOf(
                jwtProvider.extractClaims(refreshToken).getSubject()
        );

        // 회원 정보 조회
        User user = authMapper.findById(userId);

        if (user == null) {
            throw new NotRegisteredException(
                    "존재하지 않는 회원입니다."
            );
        }

        // DB에 저장된 Refresh Token 과 비교
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException(
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        // 새로운 Access Token 발급
        String newAccessToken =
                jwtProvider.generateAccessToken(user);

        // 새로운 Refresh Token 발급
        String newRefreshToken =
                jwtProvider.generateRefreshToken(user);

        // DB에 새로운 Refresh Token 저장
        authMapper.updateRefreshToken(
                user.getId(),
                newRefreshToken
        );

        // 새로운 Refresh Token 을 Cookie에도 저장
        cookieManager.setCookie(
                response,
                jwtConfig.refreshTokenCookieName(),
                newRefreshToken,
                jwtConfig.refreshTokenCookieExpiry(),
                jwtConfig.reissUri()
        );

        // 새 Access Token 반환
        return ReissueTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Cookie에서 Refresh Token 조회
        String refreshToken = jwtProvider
                .extractRefreshToken(request)
                .orElseThrow(() ->
                        new InvalidTokenException("Refresh Token이 없습니다.")
                );

        // 회원 조회
        if (refreshToken != null) {
            Long userId = Long.valueOf(
                jwtProvider.extractClaims(refreshToken).getSubject()
            );


            User user = authMapper.findById(userId);

            if (user == null) {
                throw new NotRegisteredException(
                    "존재하지 않는 회원입니다."
                );
            }

            if (user != null) {
                // DB의 Refresh Token 삭제
                authMapper.deleteRefreshToken(user.getId());
            }
        }

        // Cookie 삭제
        cookieManager.deleteCookie(
                response,
                jwtConfig.refreshTokenCookieName(),
                jwtConfig.reissUri()
        );
    }

    /**
     * 회원가입 처리
     * - 이메일 중복 확인
     * - 비밀번호 암호화
     * - 회원 정보 저장
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(
            RegisterRequest registerRequest
    ) {
        // 이메일 중복 여부 확인
        int emailCount =
                authMapper.countByEmail(registerRequest.email());

        // 이미 사용 중인 이메일이면 회원가입 실패
        if (emailCount > 0) {
            throw new DuplicatedRecordException(
                    "이미 사용중인 이메일입니다."
            );
        }

        // 비밀번호를 암호화하여 회원 객체 생성
        User user = User.builder()
                .email(registerRequest.email())
                .password(
                        passwordEncoder.encode(
                                registerRequest.password()
                        )
                )
                .build();

        // 회원 정보 저장
        int result = authMapper.save(user);

        // 저장이 실패할 경우
        if (result != 1) {
            throw new RuntimeException(
                    "회원가입 중 문제가 발생했습니다."
            );
        }

        // 저장된 회원 정보 조회
        User savedUser =
                authMapper.findById(user.getId());

        if (savedUser == null) {
            throw new RuntimeException(
                    "가입된 회원 정보를 확인할 수 없습니다."
            );
        }

        // 회원가입 결과 반환
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .build();
    }
   
}
