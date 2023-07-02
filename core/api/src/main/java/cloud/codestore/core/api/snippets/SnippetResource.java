package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.api.languages.LanguageResource;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

public class SnippetResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "snippet";

    private String title;
    private String description;
    private String code;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private Relationship language;

    SnippetResource(@Nonnull Snippet snippet) {
        super(RESOURCE_TYPE, snippet.getId());
        setSelfLink(getLink(getId()));

        this.title = snippet.getTitle();
        this.description = snippet.getDescription();
        this.code = snippet.getCode();
        this.created = snippet.getCreated();
        this.modified = snippet.getModified();
        this.language = new Relationship(LanguageResource.getLink(snippet.getLanguage()));
    }

    @JsonCreator
    private SnippetResource(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("code") String code,
            @JsonProperty("language") Relationship language
    ) {
        super(RESOURCE_TYPE);
        this.title = title;
        this.description = description;
        this.code = code;
        this.language = language;
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

    @JsonGetter("created")
    public OffsetDateTime getCreated() {
        return created;
    }

    @JsonGetter("modified")
    public OffsetDateTime getModified() {
        return modified;
    }

    @JsonGetter("language")
    public Relationship getLanguage() {
        return language;
    }

    /**
     * @param snippetId the id of a snippet.
     * @return the URI to the corresponding snippet resource.
     */
    private static String getLink(@Nonnull String snippetId) {
        return UriFactory.createUri(SnippetCollectionResource.PATH + "/" + snippetId);
    }
}
