package cloud.codestore.core.api;

import cloud.codestore.core.api.snippets.SnippetResource;
import cloud.codestore.core.api.tags.TagResource;
import cloud.codestore.jsonapi.JsonApiObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ApiConfiguration {
    /**
     * @return an {@link ObjectMapper} for serializing and deserializing JSON:API DTOs.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        var objectMapper = new JsonApiObjectMapper();
        objectMapper.registerResourceType(SnippetResource.RESOURCE_TYPE, SnippetResource.class);
        objectMapper.registerResourceType(TagResource.RESOURCE_TYPE, TagResource.class);
        enableOffsetDateTimeSupport(objectMapper);
        return objectMapper;
    }

    private void enableOffsetDateTimeSupport(ObjectMapper objectMapper) {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
    }
}
