package com.github.scheduler.global.config.auth.filter;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 1. Request Header에서 토큰을 꺼낸다.
        String token = resolveToken(request);


        // 2. validateToken으로 토큰 유효성 검사한 후, 정상적인 토큰이면 Authentication을 가져와서 SecurityContext에 저장한다.
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Security Context에 '{}' 인증 정보를 저장했습니다. uri: {}", authentication.getName(), requestURI);

        } else {
            log.debug("유효한 JWT 토큰이 없습니다, 요청 uri: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(bearerToken)) {
            log.warn("Authorization 헤더가 없음");
            return null;
        }

        if (!bearerToken.toLowerCase().startsWith("bearer ")) {
            log.warn("Bearer 타입이 아님: {}", bearerToken);
            return null;
        }

        String token = bearerToken.substring(7);
        log.info("추출된 토큰: {}", token);
        return token;
    }
}
