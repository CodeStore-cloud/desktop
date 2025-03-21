package cloud.codestore.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a code snippet.
 */
public class Snippet {
    private final String id;
    private final String title;
    private final String description;
    private final String code;
    private final List<String> tags;
    private final Language language;
    private final OffsetDateTime created;
    private final OffsetDateTime modified;

    Snippet(
            @Nonnull String id,
            @Nonnull String title,
            @Nonnull String description,
            @Nonnull String code,
            @Nonnull List<String> tags,
            @Nonnull Language language,
            @Nonnull OffsetDateTime created,
            @Nullable OffsetDateTime modified
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.code = code;
        this.tags = tags;
        this.language = language;
        this.created = created.truncatedTo(ChronoUnit.SECONDS)
                              .withOffsetSameInstant(ZoneOffset.UTC);
        this.modified = modified == null ? null : modified.truncatedTo(ChronoUnit.SECONDS)
                                                          .withOffsetSameInstant(ZoneOffset.UTC);
    }

    public static SnippetBuilder builder() {
        return new SnippetBuilder();
    }

    @Nonnull
    public String getId() {
        return id;
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
    public List<String> getTags() {
        return tags;
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

    @Nonnull
    public Optional<OffsetDateTime> getOptionalModified() {
        return Optional.ofNullable(modified);
    }

    @Nonnull
    public Set<Permission> getPermissions() {
        return Set.of(Permission.UPDATE, Permission.DELETE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        return Objects.equals(this.id, ((Snippet) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
