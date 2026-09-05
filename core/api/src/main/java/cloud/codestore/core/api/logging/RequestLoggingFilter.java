package cloud.codestore.core.api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = String.valueOf(requestCounter.incrementAndGet());
        MDC.put("requestId", requestId);

        try {
            LOGGER.debug("===== ERROR ===============");
            LOGGER.debug("Request: {} {}", request.getMethod(), getRequestUrl(request));
            filterChain.doFilter(request, response);
            LOGGER.debug("Response: {}", response.getStatus());
            LOGGER.debug("===========================");
            if (response.getStatus() < HttpServletResponse.SC_BAD_REQUEST) {
                BufferedRequestAppender.getInstance().flushSuccessfulRequest(requestId);
            } else {
                BufferedRequestAppender.getInstance().flushFailedRequest(requestId);
            }
        } finally {
            MDC.remove("requestId");
        }
    }

    private String getRequestUrl(HttpServletRequest request) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString == null ? path : path + "?" + queryString;
    }
}
