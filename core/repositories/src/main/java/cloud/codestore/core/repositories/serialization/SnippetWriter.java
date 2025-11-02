package cloud.codestore.core.repositories.serialization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
public class SnippetWriter {
    private final ObjectMapper objectMapper;

    SnippetWriter(@Qualifier("snippetMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(Snippet snippet, File file) {
        try {
            file.write(stringify(snippet));
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "file.couldNotSave", file);
        }
    }

    public String stringify(Snippet snippet) throws JsonProcessingException {
        Map<String, Object> additionalProperties;
        if (snippet instanceof ExtendedSnippet extendedSnippet) {
            additionalProperties = extendedSnippet.getAdditionalProperties();
        } else {
            additionalProperties = Collections.emptyMap();
        }

        var dto = new PersistentSnippetDto(
                snippet.getLanguage().getId(),
                snippet.getTitle(),
                snippet.getDescription(),
                snippet.getCode(),
                snippet.getTags(),
                snippet.getCreated().toString(),
                Objects.toString(snippet.getModified(), null),
                additionalProperties
        );

        return objectMapper.writeValueAsString(dto);
    }
}
