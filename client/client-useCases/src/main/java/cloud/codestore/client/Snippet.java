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
            @Nonnull String title,
            @Nonnull String description,
            @Nonnull String code,
            @Nonnull Language language,
            @Nonnull OffsetDateTime created,
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

    @Nonnull
    public String getUri() {
        return uri;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public String getCode() {
        return code;
    }

    @Nonnull
    public Language getLanguage() {
        return language;
    }

    @Nonnull
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
