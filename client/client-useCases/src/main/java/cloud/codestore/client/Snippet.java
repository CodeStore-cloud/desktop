package cloud.codestore.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a code snippet.
 */
public class Snippet {
    private final String id;
    private final String uri;
    private final String title;
    private final String description;
    private final String code;
    private final Language language;
    private final List<String> tags;
    private final OffsetDateTime created;
    private final OffsetDateTime modified;
    private final Set<Permission> permissions;

    Snippet(
            @Nonnull String id,
            @Nonnull String uri,
            @Nullable String title,
            @Nullable String description,
            @Nullable String code,
            @Nullable Language language,
            @Nullable List<String> tags,
            @Nullable OffsetDateTime created,
            @Nullable OffsetDateTime modified,
            @Nullable Set<Permission> permissions
    ) {
        this.id = id;
        this.uri = uri;
        this.title = title;
        this.description = description;
        this.code = code;
        this.language = language;
        this.tags = Objects.requireNonNullElseGet(tags, Collections::emptyList);
        this.created = created;
        this.modified = modified;
        this.permissions = Objects.requireNonNullElseGet(permissions, Collections::emptySet);
    }

    public static SnippetBuilder builder() {
        return new SnippetBuilder();
    }

    @Nonnull
    public String getId() {
        return id;
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

    @Nonnull
    public List<String> getTags() {
        return tags;
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

    @Nonnull
    public Set<Permission> getPermissions() {
        return permissions;
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
