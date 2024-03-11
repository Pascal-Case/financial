package com.springboot.financial.security;

import com.springboot.financial.exception.impl.JwtValidationException;
import com.springboot.financial.exception.impl.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 헤더에서 토큰 가져옴
            String token = this.resolveTokenFromRequest(request);

            // 토큰 검증
            if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
                Authentication authToken = this.tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authToken); // 인증정보 담음
            }
        } catch (TokenExpiredException e) {
            log.error("TokenExpiredException occurred: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        } catch (JwtValidationException e) {
            log.error("JwtValidationException occurred: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // 필터 체인
        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int httpStatus, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        String jsonResponse = String.format("{\"code\": %d, \"message\": \"%s\"}", httpStatus, message);
        response.getWriter().write(jsonResponse);
    }
}
