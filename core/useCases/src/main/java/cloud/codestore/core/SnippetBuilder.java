package cloud.codestore.core;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
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
    private List<String> tags;
    private OffsetDateTime created;
    private OffsetDateTime modified;

    protected SnippetBuilder() {}

    protected SnippetBuilder(Snippet snippet) {
        id(snippet.getId());
        language(snippet.getLanguage());
        title(snippet.getTitle());
        description(snippet.getDescription());
        code(snippet.getCode());
        tags(snippet.getTags());
        created(snippet.getCreated());
        modified(snippet.getModified());
    }

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

    public SnippetBuilder tags(List<String> tags) {
        this.tags = tags;
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
                tags == null ? Collections.emptyList() : Collections.unmodifiableList(tags),
                Objects.requireNonNullElseGet(language, Language::getDefault),
                Objects.requireNonNullElseGet(created, OffsetDateTime::now),
                modified
        );
    }
}
