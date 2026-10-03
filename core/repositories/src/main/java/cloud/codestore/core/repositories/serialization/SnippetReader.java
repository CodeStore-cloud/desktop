package cloud.codestore.core.repositories.serialization;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.synchronization.RemoteFile;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronizations;
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
    private final SnippetSynchronizations syncStates;

    SnippetReader(@Qualifier("snippetMapper") ObjectMapper objectMapper, SnippetSynchronizations syncStates) {
        this.objectMapper = objectMapper;
        this.syncStates = syncStates;
    }

    public Snippet read(File file) {
        try {
            String snippetId = SnippetFileHelper.getSnippetId(file);
            return read(snippetId, file.readOrElse("{}"));
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "file.invalidFormat", file.path());
        }
    }

    public Snippet read(RemoteFile file) {
        try {
            String snippetId = SnippetFileHelper.getSnippetId(file.getName());
            return read(snippetId, file.read());
        } catch (JsonProcessingException exception) {
            throw new RepositoryException(exception, "cloud.file.invalidFormat", file.getPath());
        }
    }

    private Snippet read(String snippetId, String fileContent) throws JsonProcessingException {
        var dto = objectMapper.readValue(fileContent, PersistentSnippetDto.class);
        return new ExtendedSnippetBuilder(dto.getAdditionalProperties())
                .id(snippetId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .code(dto.getCode())
                .tags(dto.getTags())
                .language(getLanguageById(dto.getLanguage()))
                .created(parseDateTime(dto.getCreated()))
                .modified(parseDateTime(dto.getModified()))
                .synchronizationState(syncStates.getOptional(snippetId).orElse(null))
                .build();
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
