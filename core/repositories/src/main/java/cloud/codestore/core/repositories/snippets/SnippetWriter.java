package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
class SnippetWriter {
    private final ObjectMapper objectMapper;

    SnippetWriter(@Qualifier("snippetMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void write(Snippet snippet, File file) {
        write(snippet, Map.of(), file);
    }

    void write(Snippet snippet, Map<String, Object> additionalProperties, File file) {
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

        try {
            file.write(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "file.couldNotSave", file);
        }
    }
}
