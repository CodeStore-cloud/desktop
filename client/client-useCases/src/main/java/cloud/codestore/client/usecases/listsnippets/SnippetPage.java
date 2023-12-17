package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SnippetPage {
    private final List<SnippetListItem> snippets;
    private final String nextPage;

    public SnippetPage(
            @Nonnull List<SnippetListItem> snippets,
            @Nullable String nextPage
    ) {

        this.snippets = snippets;
        this.nextPage = nextPage;
    }

    @Nonnull
    public List<SnippetListItem> getSnippets() {
        return snippets;
    }

    @Nonnull
    public Optional<String> getNextPageUri() {
        return Optional.ofNullable(nextPage);
    }
}
