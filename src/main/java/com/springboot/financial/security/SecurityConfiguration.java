package com.springboot.financial.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final TokenProvider tokenProvider;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)

                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // H2 콘솔 사용을 위한 설정

                .authorizeHttpRequests(request -> request
                        .requestMatchers("/", "/auth/signup", "/auth/signin").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // 개발 관련
                )
                .authorizeHttpRequests(request -> request.anyRequest().authenticated())

                .addFilterBefore(
                        this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );


        return http.build();
    }
}
