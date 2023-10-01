package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Language;

import java.util.List;

public record UpdatedSnippetDto(
        String id,
        Language language,
        String title,
        String code,
        List<String> tags,
        String description
) {}
