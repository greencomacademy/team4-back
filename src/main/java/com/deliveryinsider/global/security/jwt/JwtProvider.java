package com.deliveryinsider.global.security.jwt;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.MalformedKeyException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


import com.deliveryinsider.domain.user.entities.User;
import com.deliveryinsider.global.errors.custom.InvalidTokenException;
import com.deliveryinsider.global.security.cookie.CookieManager;


@Component
public class JwtProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    private final CookieManager cookieManager;

    public JwtProvider(
            JwtConfig jwtConfig,
            CookieManager cookieManager
    ) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtConfig.secret())
        );
        this.cookieManager = cookieManager;
    }

    /**
     * Access Token과 Refresh Token을 생성하는 공통 메서드
     */
    private String generateToken(User user, long ttl) {
        Date now = new Date();

        return Jwts.builder()
                .header()
                .type(jwtConfig.type())
                .and()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtConfig.issuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(User user) {
        return generateToken(
                user,
                jwtConfig.accessTokenExpiry()
        );
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(User user) {
        return generateToken(
                user,
                jwtConfig.refreshTokenExpiry()
        );
    }

    /**
     * 요청의 Cookie에서 Refresh Token을 추출한다.
     */
    public Optional<String> extractRefreshToken(
            HttpServletRequest request
    ) {
        return cookieManager
                .getCookie(
                        request,
                        jwtConfig.refreshTokenCookieName()
                )
                .map(Cookie::getValue);
    }

    /**
     * Authorization 헤더에서 Access Token을 추출한다.
     */
    public Optional<String> extractAccessToken(
            HttpServletRequest request
    ) {
        String bearerToken =
                request.getHeader(jwtConfig.headerKey());

        if (
                bearerToken == null
                        || !bearerToken.startsWith(jwtConfig.scheme())
        ) {
            return Optional.empty();
        }

        String token = bearerToken
                .substring(jwtConfig.scheme().length())
                .trim();

        if (token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token);
    }

    /**
     * JWT를 검증하고 Claims를 반환한다.
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(
                    "토큰이 만료되었습니다."
            );

        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException(
                    "지원하지 않는 토큰입니다."
            );

        } catch (MalformedKeyException e) {
            throw new InvalidTokenException(
                    "토큰 형식이 올바르지 않습니다."
            );

        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(
                    "토큰 검증에 실패했습니다."
            );
        }
    }



}
