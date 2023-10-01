package cloud.codestore.core.repositories.snippets;

import java.util.List;

record PersistentSnippetDto(
        int language,
        String title,
        String description,
        String code,
        List<String> tags,
        String created,
        String modified
) {}