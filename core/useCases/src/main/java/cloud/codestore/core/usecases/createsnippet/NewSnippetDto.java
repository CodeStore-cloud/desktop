package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Language;

import java.util.List;

public record NewSnippetDto(
        Language language,
        String title,
        String code,
        List<String> tags,
        String description
) {}
