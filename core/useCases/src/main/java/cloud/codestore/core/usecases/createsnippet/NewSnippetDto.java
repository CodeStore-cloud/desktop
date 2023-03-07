package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Language;

public record NewSnippetDto(
        Language language,
        String title,
        String code,
        String description
) {}
