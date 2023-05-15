package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Language;

public record UpdatedSnippetDto(
        String id,
        Language language,
        String title,
        String code,
        String description
) {}
