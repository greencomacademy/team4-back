package com.deliveryinsider.global.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieManager {

    public void setCookie(
        HttpServletResponse response,
        String name,
        String value,
        int maxAge,
        String path
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(false)      // 로컬 개발은 http라 false
            .sameSite("Lax")    // localhost 개발에서는 Lax가 무난
            .path(path)
            .maxAge(maxAge)
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
        );
    }

    public Optional<Cookie> getCookie(
        HttpServletRequest request,
        String name
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(name))
            .findFirst();
    }

    public void deleteCookie(
        HttpServletResponse response,
        String name,
        String path
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path(path)
            .maxAge(0)
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
        );
    }
}
