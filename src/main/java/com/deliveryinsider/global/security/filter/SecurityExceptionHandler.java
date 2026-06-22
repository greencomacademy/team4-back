package com.deliveryinsider.global.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class SecurityExceptionHandler
    implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public SecurityExceptionHandler(
        @Qualifier("handlerExceptionResolver")
        HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    /**
     * 인증되지 않은 사용자가 인증 필수 API에 접근했을 때 실행
     * 일반적으로 HTTP 401 상황
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) {
        handlerExceptionResolver.resolveException(
            request,
            response,
            null,
            authException
        );
    }

    /**
     * 인증은 되었지만 권한이 부족할 때 실행
     * 일반적으로 HTTP 403 상황
     */
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) {
        handlerExceptionResolver.resolveException(
            request,
            response,
            null,
            accessDeniedException
        );
    }
}
