package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Language;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public record UpdatedSnippetDto(
        String id,
        Language language,
        String title,
        String code,
        List<String> tags,
        String description
) {
    @Nonnull
    Language languageOrElse(@Nonnull Language language) {
        return Optional.ofNullable(this.language).orElse(language);
    }

    @Nonnull
    String titleOrElse(@Nonnull String title) {
        return Optional.ofNullable(this.title).orElse(title);
    }

    @Nonnull
    String descriptionOrElse(@Nonnull String description) {
        return Optional.ofNullable(this.description).orElse(description);
    }

    @Nonnull
    String codeOrElse(@Nonnull String code) {
        return Optional.ofNullable(this.code).orElse(code);
    }

    @Nonnull
    List<String> tagsOrElse(@Nonnull List<String> tags) {
        return Optional.ofNullable(this.tags).orElse(tags);
    }
}
