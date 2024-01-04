package cloud.codestore.client.repositories.root;

import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the root resource of the {CodeStore} Core API.
 */
public class RootResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "core";

    private String snippetsUrl;
    private String languagesUrl;
    private String tagsUrl;

    @JsonCreator
    RootResource(
            @JsonProperty("snippets") Relationship snippetsRelationship,
            @JsonProperty("languages") Relationship languagesRelationship,
            @JsonProperty("tags") Relationship tagsRelationship
    ) {
        super(RESOURCE_TYPE);
        this.snippetsUrl = snippetsRelationship.getRelatedResourceLink();
        this.languagesUrl = languagesRelationship.getRelatedResourceLink();
        this.tagsUrl = tagsRelationship.getRelatedResourceLink();
    }

    public String getSnippetsUrl() {
        return snippetsUrl;
    }

    public String getLanguagesUrl() {
        return languagesUrl;
    }

    public String getTagsUrl() {
        return tagsUrl;
    }
}
