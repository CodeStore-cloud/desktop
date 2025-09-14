package cloud.codestore.core.repositories.snippets;

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
class SnippetReader {
    private final ObjectMapper objectMapper;

    SnippetReader(@Qualifier("snippetMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Snippet read(File file) {
        var dto = readDto(file);
        return Snippet.builder()
                      .id(SnippetFileHelper.getSnippetId(file))
                      .title(dto.title())
                      .description(dto.description())
                      .code(dto.code())
                      .tags(dto.tags())
                      .language(getLanguageById(dto.language()))
                      .created(parseDateTime(dto.created()))
                      .modified(parseDateTime(dto.modified()))
                      .build();
    }

    private PersistentSnippetDto readDto(File file) {
        try {
            String fileContent = file.readOrElse("{}");
            return objectMapper.readValue(fileContent, PersistentSnippetDto.class);
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "file.invalidFormat", file.path());
        }
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
