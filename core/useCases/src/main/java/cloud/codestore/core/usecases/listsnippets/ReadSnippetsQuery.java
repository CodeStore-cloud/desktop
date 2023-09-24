package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;
import java.util.List;

public interface ReadSnippetsQuery {
    List<Snippet> readSnippets(@Nonnull FilterProperties filterProperties);
}
