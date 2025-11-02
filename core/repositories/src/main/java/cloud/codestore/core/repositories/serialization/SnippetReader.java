package cloud.codestore.core.repositories.serialization;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class SnippetReader {
    private final ObjectMapper objectMapper;

    SnippetReader(@Qualifier("snippetMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Snippet read(File file) {
        try {
            String snippetId = SnippetFileHelper.getSnippetId(file);
            return read(snippetId, file.readOrElse("{}"));
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "file.invalidFormat", file.path());
        }
    }

    public Snippet read(String snippetId, String fileContent) throws JsonProcessingException {
        var dto = parse(fileContent);
        return new ExtendedSnippetBuilder(dto.getAdditionalProperties())
                .id(snippetId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .code(dto.getCode())
                .tags(dto.getTags())
                .language(getLanguageById(dto.getLanguage()))
                .created(parseDateTime(dto.getCreated()))
                .modified(parseDateTime(dto.getModified()))
                .build();
    }

    private PersistentSnippetDto parse(String fileContent) throws JsonProcessingException {
        return objectMapper.readValue(fileContent, PersistentSnippetDto.class);
    }

    @Nullable
    private Language getLanguageById(int languageId) {
        for (Language language : Language.values()) {
            if (language.getId() == languageId)
                return language;
        }

        return null;
    }

    @Nullable
    private OffsetDateTime parseDateTime(@Nullable String timestamp) {
        return Optional.ofNullable(timestamp)
                       .map(OffsetDateTime::parse)
                       .orElse(null);
    }
}
