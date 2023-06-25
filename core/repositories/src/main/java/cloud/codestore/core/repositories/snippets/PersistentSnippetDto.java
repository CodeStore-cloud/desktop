package cloud.codestore.core.repositories.snippets;

record PersistentSnippetDto(
        int language,
        String title,
        String description,
        String code,
        String created,
        String modified
) {}