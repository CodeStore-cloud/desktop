package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.repositories.ResourceMetaInfo;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class SnippetResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "snippet";

    private String title;
    private String description;
    private String code;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private Relationship language;
    private Relationship tags;

    @JsonCreator
    public SnippetResource(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("code") String code,
            @JsonProperty("created") String created,
            @JsonProperty("modified") String modified,
            @JsonProperty("language") Relationship language,
            @JsonProperty("tags") Relationship tags
    ) {
        super(RESOURCE_TYPE);
        this.title = title;
        this.description = description;
        this.code = code;
        this.created = created == null ? null : OffsetDateTime.parse(created);
        this.modified = modified == null ? null : OffsetDateTime.parse(modified);
        this.language = language;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public OffsetDateTime getModified() {
        return modified;
    }

    public Relationship getLanguage() {
        return language;
    }

    public Relationship getTags() {
        return tags;
    }

    @Override
    public ResourceMetaInfo getMeta() {
        return (ResourceMetaInfo) super.getMeta();
    }
}
