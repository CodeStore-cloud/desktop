package cloud.codestore.client.usecases.createsnippet;


import cloud.codestore.client.Language;

import java.util.List;

public record NewSnippetDto(
        String title,
        String description,
        Language language,
        String code,
        List<String> tags
) {}
