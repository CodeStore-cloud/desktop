package cloud.codestore.core.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The access-token provider")
class AccessTokenProviderTest {
    @Test
    @DisplayName("generates a non-empty token")
    void generateToken() {
        String token = new AccessTokenProvider().accessToken();
        assertThat(token).isNotNull().isNotEmpty();
    }
}