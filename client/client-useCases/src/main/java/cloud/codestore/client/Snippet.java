package cloud.codestore.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents a code snippet.
 */
public class Snippet {
    private final String uri;
    private final String title;
    private final String description;
    private final String code;
    private final Language language;
    private final OffsetDateTime created;
    private final OffsetDateTime modified;

    Snippet(
            @Nonnull String uri,
            @Nullable String title,
            @Nullable String description,
            @Nullable String code,
            @Nullable Language language,
            @Nullable OffsetDateTime created,
            @Nullable OffsetDateTime modified
    ) {
        this.uri = uri;
        this.title = title;
        this.description = description;
        this.code = code;
        this.language = language;
        this.created = created;
        this.modified = modified;
    }

    public static SnippetBuilder builder() {
        return new SnippetBuilder();
    }

    @Nonnull
    public String getUri() {
        return uri;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    @Nullable
    public Language getLanguage() {
        return language;
    }

    @Nullable
    public OffsetDateTime getCreated() {
        return created;
    }

    @Nullable
    public OffsetDateTime getModified() {
        return modified;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        return Objects.equals(this.uri, ((Snippet) obj).uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
