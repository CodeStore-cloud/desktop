package cloud.codestore.client;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * A builder for dynamically creating {@link Snippet code snippets}.
 */
public class SnippetBuilder {
    private String id = "";
    private String uri = "";
    private Language language;
    private String title;
    private String description;
    private String code;
    private List<String> tags;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private Set<Permission> permissions;

    public SnippetBuilder id(String id) {
        this.id = id;
        return this;
    }

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

    public SnippetBuilder permissions(@Nonnull Set<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    public Snippet build() {
        return new Snippet(id, uri, title, description, code, language, tags, created, modified, permissions);
    }
}
