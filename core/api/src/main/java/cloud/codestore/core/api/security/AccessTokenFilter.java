package cloud.codestore.core.api.security;

import cloud.codestore.core.api.ErrorResponseBuilder;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@ConditionalOnProperty(name = "server.authentication.required", havingValue = "true", matchIfMissing = true)
class AccessTokenFilter extends OncePerRequestFilter {
    private final String accessToken;
    private final ObjectMapper objectMapper;

    @Autowired
    public AccessTokenFilter(
            @Qualifier("accessToken") String accessToken,
            ObjectMapper objectMapper
    ) {
        this.accessToken = accessToken;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            validateAccessToken(request);
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException exception) {
            ErrorObject responseObject = createError(exception.getMessage());
            sendError(response, responseObject);
        }
    }

    private void validateAccessToken(HttpServletRequest request) throws AccessDeniedException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (!Objects.equals(token, accessToken)) {
                throw new AccessDeniedException("authentication.invalidToken");
            }
        } else {
            throw new AccessDeniedException("authentication.missingToken");
        }
    }

    private void sendError(HttpServletResponse response, ErrorObject responseObject) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE);

        String responseBody = objectMapper.writeValueAsString(new ErrorDocument(responseObject));
        response.getOutputStream().write(responseBody.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    private ErrorObject createError(String message) {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        return builder.createError("ACCESS_DENIED", "authentication.title", message);
    }

    /**
     * Exception in case the client has not provided a valid access token.
     */
    private static class AccessDeniedException extends Exception {
        AccessDeniedException(String messageKey) {
            super(messageKey);
        }
    }
}
