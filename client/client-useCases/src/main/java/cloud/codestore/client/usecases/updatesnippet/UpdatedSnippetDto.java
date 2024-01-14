package cloud.codestore.client.usecases.updatesnippet;

import cloud.codestore.client.Language;

import java.util.List;

public record UpdatedSnippetDto(
        String id,
        String uri,
        String title,
        String description,
        Language language,
        String code,
        List<String> tags
) {}
