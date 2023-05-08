package cloud.codestore.core;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A builder for dynamically creating {@link Snippet code snippets}.
 */
public class SnippetBuilder {
    private String id;
    private Language language;
    private String title;
    private String description;
    private String code;
    private OffsetDateTime created;
    private OffsetDateTime modified;

    public SnippetBuilder id(String id) {
        this.id = id;
        return this;
    }

    public SnippetBuilder title(String title) {
        this.title = title;
        return this;
    }

    public SnippetBuilder language(Language language) {
        this.language = language;
        return this;
    }

    public SnippetBuilder description(String description) {
        this.description = description;
        return this;
    }

    public SnippetBuilder code(String code) {
        this.code = code;
        return this;
    }

    public SnippetBuilder created(OffsetDateTime created) {
        this.created = created;
        return this;
    }

    public SnippetBuilder modified(OffsetDateTime modified) {
        this.modified = modified;
        return this;
    }

    public Snippet build() {
        return new Snippet(
                id,
                Objects.requireNonNullElse(title, ""),
                Objects.requireNonNullElse(description, ""),
                Objects.requireNonNullElse(code, ""),
                Objects.requireNonNullElseGet(language, Language::getDefault),
                Objects.requireNonNullElseGet(created, OffsetDateTime::now),
                modified
        );
    }
}
