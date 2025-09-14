package cloud.codestore.core.api.tags;

import cloud.codestore.core.api.snippets.SnippetCollectionResource;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TagResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "tag";

    private Relationship snippets;

    @JsonCreator
    TagResource(@Nonnull @JsonProperty("name") String tag) {
        super(RESOURCE_TYPE, tag);
        setSelfLink(createLink(tag));
        snippets = new Relationship(SnippetCollectionResource.createLink(tag));
    }

    @JsonGetter("name")
    public String getName() {
        return getId();
    }

    @JsonGetter("snippets")
    public Relationship getSnippets() {
        return snippets;
    }

    private static String createLink(String tag) {
        return TagCollectionResource.createLink() + "/" + URLEncoder.encode(tag, StandardCharsets.UTF_8);
    }
}
