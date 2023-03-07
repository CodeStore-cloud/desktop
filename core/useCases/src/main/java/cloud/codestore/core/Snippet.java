package cloud.codestore.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a code snippet.
 */
public record Snippet(
        String id,
        Language language,
        String title,
        String description,
        String code,
        OffsetDateTime created,
        OffsetDateTime modified
) {
    public Snippet(
            @Nonnull String id,
            @Nonnull Language language,
            @Nonnull String title,
            @Nonnull String description,
            @Nonnull String code,
            @Nonnull OffsetDateTime created,
            @Nullable OffsetDateTime modified
    ) {
        this.id = id;
        this.language = language;
        this.title = title;
        this.description = description;
        this.code = code;
        this.created = created.truncatedTo(ChronoUnit.SECONDS);
        this.modified = modified == null ? null : modified.truncatedTo(ChronoUnit.SECONDS);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        return Objects.equals(this.id, ((Snippet) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
