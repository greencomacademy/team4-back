package com.deliveryinsider.global.security.filter;

import com.deliveryinsider.global.config.CorsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/test").permitAll()
                        .requestMatchers("/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/reissue-token").permitAll()

                        // main 단계에서는 개발 편의를 위해 일단 전체 허용
                        .anyRequest().permitAll()
                )
                .build();
    }
}
