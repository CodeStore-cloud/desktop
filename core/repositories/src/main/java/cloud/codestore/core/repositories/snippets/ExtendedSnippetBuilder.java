package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * A builder for creating snippets that may contain additional properties that are not supported by this client yet.
 */
class ExtendedSnippetBuilder extends SnippetBuilder {
    private final Map<String, Object> additionalProperties;

    ExtendedSnippetBuilder(@Nonnull Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    ExtendedSnippetBuilder(@Nonnull ExtendedSnippet snippet) {
        super(snippet);
        additionalProperties = snippet.getAdditionalProperties();
    }

    @Override
    public Snippet build() {
        Snippet baseSnippet = super.build();
        return additionalProperties.isEmpty() ? baseSnippet : new ExtendedSnippet(baseSnippet, additionalProperties);
    }
}
