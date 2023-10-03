package cloud.codestore.core.api.tags;

import cloud.codestore.core.api.snippets.SnippetCollectionResource;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

import javax.annotation.Nonnull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TagResource extends ResourceObject {
    private static final String RESOURCE_TYPE = "tag";

    private Relationship snippets;

    TagResource(@Nonnull String tag) {
        super(RESOURCE_TYPE, tag);
        setSelfLink(getLink(tag));
        snippets = new Relationship(SnippetCollectionResource.getLink(tag));
    }

    @JsonGetter("name")
    public String getName() {
        return getId();
    }

    @JsonGetter("snippets")
    public Relationship getSnippets() {
        return snippets;
    }

    private static String getLink(String tag) {
        return TagCollectionResource.getLink() + "/" + URLEncoder.encode(tag, StandardCharsets.UTF_8);
    }
}
