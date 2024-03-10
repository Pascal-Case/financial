package com.springboot.financial.security;

import com.springboot.financial.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class TokenProvider {
    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간

    private final MemberService memberService;


    private final SecretKey secretKey;

    public TokenProvider(
            MemberService memberService,
            @Value("${spring.jwt.secret}")
            String secretKey) {
        this.memberService = memberService;
        this.secretKey = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS512.key().build().getAlgorithm()
        );
        log.debug("TokenProvider initialized with given secret key.");
    }

    public String generateToken(String username, List<String> roles) {
        log.info("Generating token for username: {}", username);
        var now = new Date(System.currentTimeMillis());
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        String token = Jwts.builder()
                .subject(username)
                .claim(KEY_ROLES, roles)
                .issuedAt(now)
                .expiration(expiredDate)
                .signWith(this.secretKey)
                .compact();
        log.debug("Token generated successfully for username: {}", username);
        return token;
    }

    public Authentication getAuthentication(String jwt) {
        log.debug("Getting authentication for user: {}", getUsername(jwt));
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    public Boolean validateToken(String token) {
        Claims claims = parseClaims(token);
        boolean isExpired = claims.getExpiration().before(new Date());
        if (isExpired) {
            log.info("Token expired for subject: {}", claims.getSubject());
            return false;
        }
        return true;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.info("Parsing claims from expired token for subject: {}", e.getClaims().getSubject());
            return e.getClaims();
        }
    }
}
