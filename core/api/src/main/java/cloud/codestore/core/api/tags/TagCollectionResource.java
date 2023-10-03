package cloud.codestore.core.api.tags;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class TagCollectionResource extends ResourceCollectionDocument<TagResource> {
    static final String PATH = "/tags";

    TagCollectionResource(@Nonnull Collection<String> tags) {
        super(convertToTagResource(tags));
        setSelfLink(getLink());
    }

    /**
     * @return the URI to the tag collection resource.
     */
    static String getLink() {
        return UriFactory.createUri(PATH);
    }

    /**
     * @param snippetId the ID of a code snippet. Must not be {@code null}.
     * @return the URI to the tag collection resource filtered by the given snippet ID.
     */
    public static String getLink(@Nonnull String snippetId) {
        return UriFactory.createUri(PATH, Map.of("filter[snippet]", snippetId));
    }

    private static TagResource[] convertToTagResource(Collection<String> tags) {
        return tags.stream()
                   .map(TagResource::new)
                   .toArray(TagResource[]::new);
    }
}
