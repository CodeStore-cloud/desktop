package cloud.codestore.core.repositories.serialization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * There may be a time when the JSON files contain additional properties (as a result of synchronization) that are not
 * supported by this client yet. In that case, those properties need to be preserved.
 */
class ExtendedSnippet extends Snippet {
    private final Map<String, Object> additionalProperties;

    ExtendedSnippet(
            @Nonnull Snippet snippet,
            @Nonnull Map<String, Object> additionalProperties
    ) {
        super(
                snippet.getId(),
                snippet.getTitle(),
                snippet.getDescription(),
                snippet.getCode(),
                snippet.getTags(),
                snippet.getLanguage(),
                snippet.getCreated(),
                snippet.getModified()
        );
        this.additionalProperties = additionalProperties;
    }

    @Override
    public SnippetBuilder toBuilder() {
        return new ExtendedSnippetBuilder(this);
    }

    @Nonnull
    Map<String, Object> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }
}
