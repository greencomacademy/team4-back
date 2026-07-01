package com.deliveryinsider.global.security.filter;

import com.deliveryinsider.global.config.CorsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CorsConfig corsConfig;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http
    ) throws Exception {

        return http
            // JWT 방식이므로 서버 세션을 사용하지 않음
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // REST API + JWT 방식 기본 설정
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // 기존 CorsConfig 사용
            .cors(cors ->
                cors.configurationSource(
                    corsConfig.corsConfigurationSource()
                )
            )

            // JWT 필터를 기본 로그인 필터보다 먼저 실행
            .addFilterBefore(
                tokenAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )

            // API 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/test",
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/reissue-token",
                    "/api/auth/logout"
                ).permitAll()

                // 위 주소를 제외한 API는 로그인 필요
                .anyRequest().authenticated()
            )

            // 401, 403 예외 처리 연결
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(
                    securityExceptionHandler
                )
                .accessDeniedHandler(
                    securityExceptionHandler
                )
            )

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
