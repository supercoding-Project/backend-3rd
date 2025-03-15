package com.github.scheduler.global.config.auth;
import com.github.scheduler.global.config.auth.custom.CustomUserDetailsServiceImpl;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final CustomUserDetailsServiceImpl customUserDetailsService;

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.token.access-expiration-time}")
    private Long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private Long refreshExpirationTime;


    // 토큰 생성
    public String generateToken(String email, Long expireTime) {

        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date validateTime = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validateTime)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }


    // 액세스 토큰 만들기
    public String createAccessToken(String email) {
        return generateToken(email, accessExpirationTime);
    }


    // 리프레시 토큰 만들기
    public String createRefreshToken(String email) {
        return generateToken(email, refreshExpirationTime);
    }


    // 토큰 분해하기
    public String parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }


    // 인증정보 가져오기
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.parseClaims(token));

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }


    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token.", e);

        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token.", e);

        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token.", e);

        } catch (IllegalArgumentException e) {
            log.info("JWT Claims String is empty.", e);
        }

        return false;
    }


    // 관리자 권한 부여여부
    public void setRole(String accessToken, String role) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        claims.put("roles", role);

        log.info("claims: {}", claims);

        Jwts.builder()
                .setClaims(claims)
                .compact();
    }


    // 토큰으로부터 이메일 불러오기
    public String getEmailByToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.isEmpty() ? null : claims.get("sub", String.class);
    }
}
