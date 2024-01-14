package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.repositories.language.LanguageResource;
import cloud.codestore.client.repositories.tags.TagResource;
import cloud.codestore.jsonapi.relationship.ToManyRelationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class SnippetResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "snippet";

    private String title;
    private String description;
    private String code;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private ToOneRelationship<LanguageResource> language;
    private ToManyRelationship<TagResource> tags;

    @JsonCreator
    public SnippetResource(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("code") String code,
            @JsonProperty("created") String created,
            @JsonProperty("modified") String modified,
            @JsonProperty("language") ToOneRelationship<LanguageResource> language,
            @JsonProperty("tags") ToManyRelationship<TagResource> tags
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

    SnippetResource(
            String title,
            String description,
            String code,
            ToOneRelationship<LanguageResource> language,
            ToManyRelationship<TagResource> tags
    ) {
        super(RESOURCE_TYPE);
        this.title = title;
        this.description = description;
        this.code = code;
        this.language = language;
        this.tags = tags;
    }

    SnippetResource(
            String id,
            String title,
            String description,
            String code,
            ToOneRelationship<LanguageResource> language,
            ToManyRelationship<TagResource> tags
    ) {
        super(RESOURCE_TYPE, id);
        this.title = title;
        this.description = description;
        this.code = code;
        this.language = language;
        this.tags = tags;
    }

    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }

    @JsonGetter("code")
    public String getCode() {
        return code;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public OffsetDateTime getModified() {
        return modified;
    }

    @JsonGetter("language")
    public ToOneRelationship<LanguageResource> getLanguage() {
        return language;
    }

    @JsonGetter("tags")
    public ToManyRelationship<TagResource> getTags() {
        return tags;
    }
}
