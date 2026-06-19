package com.deliveryinsider.global.security.cookie;

import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class CookieManager {
    // 쿠키 생성 및 응답에 저장
    public void setCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAge,
            String path
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    // 요청에서 쿠키 조회
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
}
