package com.deliveryinsider.global.security.filter;

import com.deliveryinsider.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityAuthenticationProvider {

    private final JwtProvider jwtProvider;

    public Authentication authentication(String token) {

        return new UsernamePasswordAuthenticationToken(
            jwtProvider.extractClaims(token),
            null,
            List.of()
        );
    }
}