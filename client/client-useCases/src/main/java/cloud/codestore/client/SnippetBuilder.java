package cloud.codestore.client;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A builder for dynamically creating {@link Snippet code snippets}.
 */
public class SnippetBuilder {
    private String uri;
    private Language language;
    private String title;
    private String description;
    private String code;
    private List<String> tags;
    private OffsetDateTime created;
    private OffsetDateTime modified;

    public SnippetBuilder uri(String uri) {
        this.uri = uri;
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
        return new Snippet(uri, title, description, code, language, tags, created, modified);
    }
}
