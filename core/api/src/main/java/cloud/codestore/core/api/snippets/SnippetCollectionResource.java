package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.api.UriFactory;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SnippetCollectionResource extends ResourceCollectionDocument<SnippetResource> {
    static final String PATH = "/snippets";

    SnippetCollectionResource(@Nonnull List<Snippet> snippets) {
        super(convertToSnippetResource(snippets));
    }

    /**
     * @return the URI to the snippet collection resource.
     */
    public static String getLink() {
        return UriFactory.createUri(PATH);
    }

    /**
     * @param language a programming language. Must not be {@code null}.
     * @return the URI to the snippet collection resource filtered by the given programming language.
     */
    public static String getLink(@Nonnull Language language) {
        return getLink(Map.of("filter[language]", language.getId()));
    }

    /**
     * @param tag a tag. Must not be {@code null}.
     * @return the URI to the snippet collection resource filtered by the given tag.
     */
    public static String getLink(@Nonnull String tag) {
        return getLink(Map.of("filter[tags]", tag));
    }

    /**
     * @param urlParameters additional URL parameters to add.
     * @return the URI to the corresponding page of the snippet collection resource.
     */
    static String getLink(@Nonnull Map<String, Object> urlParameters) {
        return UriFactory.createUri(PATH, urlParameters);
    }

    private static SnippetResource[] convertToSnippetResource(List<Snippet> snippets) {
        return snippets.stream()
                       .map(SnippetResource::new)
                       .toArray(SnippetResource[]::new);
    }
}
