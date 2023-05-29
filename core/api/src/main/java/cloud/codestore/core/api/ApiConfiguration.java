package cloud.codestore.core.api;

import cloud.codestore.core.api.root.RootController;
import cloud.codestore.jsonapi.JsonApiObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({RootController.class, UriFactory.class})
public class ApiConfiguration {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new JsonApiObjectMapper();
    }
}
