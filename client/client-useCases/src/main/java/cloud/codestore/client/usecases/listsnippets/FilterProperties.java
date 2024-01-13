package cloud.codestore.client.usecases.listsnippets;

import cloud.codestore.client.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * Provides information about how to filter the requested code snippets.
 */
public class FilterProperties {
    private Set<String> tags;
    private Language language;

    /**
     * Default filter properties
     */
    public FilterProperties() {
        this(null, null);
    }

    public FilterProperties(@Nullable Set<String> tags, @Nullable Language language) {
        this.tags = tags;
        this.language = language;
    }

    @Nonnull
    public Optional<Set<String>> getTags() {
        return tags == null || tags.isEmpty() ? Optional.empty() : Optional.of(tags);
    }

    @Nonnull
    public Optional<Language> getLanguage() {
        return Optional.ofNullable(language);
    }
}
