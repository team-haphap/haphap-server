package org.sopt.haphap.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 요청 1건당 access log 1줄. RequestIdFilter 다음에 실행되어야 requestId가 찍힘.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Set<String> EXCLUDED_EXACT = Set.of(
            "/favicon.ico",
            "/robots.txt"
    );

    private static final Set<String> EXCLUDED_PREFIX = Set.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs"
    );

    /**
     * 헬스체크/문서 요청은 초당 수십 건씩 찍혀 로그를 오염시킴.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (EXCLUDED_EXACT.contains(uri)) {
            return true;
        }
        return EXCLUDED_PREFIX.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // nanoTime 은 시스템 시계 변경(NTP 동기화 등)에 영향받지 않음
        long startNanos = System.nanoTime();

        try {
            filterChain.doFilter(request, response);

        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            logAccess(request, response.getStatus(), elapsedMs);
        }
    }

    private void logAccess(HttpServletRequest request, int status, long elapsedMs) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String path = (query == null) ? uri : uri + "?" + query;

        if (status >= 500) {
            log.error("{} {} -> {} ({}ms)", method, path, status, elapsedMs);
        } else if (status >= 400) {
            log.warn("{} {} -> {} ({}ms)", method, path, status, elapsedMs);
        } else {
            log.info("{} {} -> {} ({}ms)", method, path, status, elapsedMs);
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}