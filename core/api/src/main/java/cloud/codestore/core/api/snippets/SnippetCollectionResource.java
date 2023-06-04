package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.api.UriFactory;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;

import javax.annotation.Nonnull;
import java.util.List;

public class SnippetCollectionResource extends ResourceCollectionDocument<SnippetResource> {
    static final String PATH = "/snippets";

    SnippetCollectionResource(@Nonnull List<Snippet> snippets) {
        super(convertToSnippetResource(snippets));
    }

    /**
     * @return the URI to the snippet collection resource.
     */
    public static String getLink()
    {
        return UriFactory.createUri(PATH);
    }

    private static SnippetResource[] convertToSnippetResource(List<Snippet> snippets) {
        return snippets.stream()
                       .map(SnippetResource::new)
                       .toArray(SnippetResource[]::new);
    }
}
