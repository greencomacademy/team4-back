package com.deliveryinsider.global.security.filter;

import com.deliveryinsider.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final SecurityAuthenticationProvider securityAuthenticationProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public TokenAuthenticationFilter(
        JwtProvider jwtProvider,
        SecurityAuthenticationProvider securityAuthenticationProvider,
        @Qualifier("handlerExceptionResolver")
        HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtProvider = jwtProvider;
        this.securityAuthenticationProvider =
            securityAuthenticationProvider;
        this.handlerExceptionResolver =
            handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더에서 Access Token 추출
        Optional<String> tokenOptional =
            jwtProvider.extractAccessToken(request);

        // 2. 토큰이 있을 때만 인증 처리
        if (tokenOptional.isPresent()) {
            try {
                String token = tokenOptional.get();

                // 3. JWT 검증 후 Spring Security 인증 객체 생성
                SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                        securityAuthenticationProvider
                            .authentication(token)
                    );

            } catch (Exception e) {
                /*
                 * 필터에서 발생한 예외는 Controller까지 도달하지 않으므로
                 * HandlerExceptionResolver를 통해
                 * GlobalExceptionHandler로 전달한다.
                 */
                handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    e
                );

                return;
            }
        }

        // 4. 다음 필터 또는 Controller 실행
        filterChain.doFilter(request, response);
    }
}