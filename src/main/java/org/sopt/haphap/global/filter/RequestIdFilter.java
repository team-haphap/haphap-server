package org.sopt.haphap.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청마다 추적용 requestId를 MDC에 심는다.
 * 다른 모든 필터(Security 포함)보다 먼저 실행되어야 하므로 HIGHEST_PRECEDENCE.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "requestId";
    private static final String HEADER_NAME = "X-Request-Id";
    private static final int ID_LENGTH = 8;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = resolveRequestId(request);

        try {
            MDC.put(MDC_KEY, requestId);
            response.setHeader(HEADER_NAME, requestId);

            filterChain.doFilter(request, response);

        } finally {
            // (clear() 는 다른 컴포넌트가 넣은 MDC 값까지 지움.) 내가 넣은 것만 제거.
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * 게이트웨이/앞단 서버가 내려준 추적 ID가 있으면 승계하고, 없으면 새로 발급한다.
     */
    private String resolveRequestId(HttpServletRequest request) {
        String inbound = request.getHeader(HEADER_NAME);

        if (inbound != null && !inbound.isBlank()) {
            // 외부 입력이므로 로그 인젝션 방지를 위해 길이/문자 제한
            return sanitize(inbound);
        }
        return UUID.randomUUID().toString().substring(0, ID_LENGTH);
    }

    private String sanitize(String value) {
        String trimmed = value.length() > 64 ? value.substring(0, 64) : value;
        return trimmed.replaceAll("[^a-zA-Z0-9\\-_]", "");
    }

    /**
     * @Async, SSE 등 비동기 디스패치에서도 MDC가 유지되도록 필터를 다시 태운다.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * ERROR 디스패치(예: /error 포워딩)에서도 requestId가 찍히도록 한다.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}