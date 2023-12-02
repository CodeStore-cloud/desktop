package cloud.codestore.core.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Configuration
class AccessTokenProvider {
    @Bean("accessToken")
    public String accessToken() {
        return generateToken();
    }

    private static String generateToken() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] base64EncodedUuid = Base64.getEncoder().encode(byteBuffer.array());
        return new String(base64EncodedUuid);
    }
}
