package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

class SnippetWriter {
    private final ObjectMapper objectMapper;

    SnippetWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void write(Snippet snippet, File file) {
        var dto = new PersistentSnippetDto(
                snippet.getLanguage().getId(),
                snippet.getTitle(),
                snippet.getDescription(),
                snippet.getCode(),
                snippet.getCreated().toString(),
                Objects.toString(snippet.getModified(), null)
        );

        try {
            file.write(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "file.couldNotSave", file);
        }
    }
}
