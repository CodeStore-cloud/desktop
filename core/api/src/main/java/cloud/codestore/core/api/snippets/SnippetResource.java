package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Permission;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.api.Operation;
import cloud.codestore.core.api.ResourceMetaInfo;
import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.api.languages.LanguageResource;
import cloud.codestore.core.api.tags.TagCollectionResource;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SnippetResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "snippet";

    private String title;
    private String description;
    private String code;
    private OffsetDateTime created;
    private OffsetDateTime modified;
    private Relationship language;
    private Relationship tags;

    SnippetResource(@Nonnull Snippet snippet) {
        super(RESOURCE_TYPE, snippet.getId());
        setSelfLink(getLink(getId()));
        setMeta(createPermissionsMetaInfo(snippet.getPermissions()));

        this.title = snippet.getTitle();
        this.description = snippet.getDescription();
        this.code = snippet.getCode();
        this.created = snippet.getCreated();
        this.modified = snippet.getModified();
        this.language = new Relationship(LanguageResource.getLink(snippet.getLanguage()));
        this.tags = new Relationship(TagCollectionResource.getLink(getId()));
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

    @JsonGetter("tags")
    public Relationship getTags() {
        return tags;
    }

    /**
     * @param snippetId the id of a snippet.
     * @return the URI to the corresponding snippet resource.
     */
    private String getLink(@Nonnull String snippetId) {
        return UriFactory.createUri(SnippetCollectionResource.PATH + "/" + snippetId);
    }

    @Nullable
    private ResourceMetaInfo createPermissionsMetaInfo(Set<Permission> permissions) {
        List<Operation> operations = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission == Permission.UPDATE) {
                operations.add(new Operation("updateSnippet", HttpMethod.PATCH.name(), getSelfLink()));
            } else if (permission == Permission.DELETE) {
                operations.add(new Operation("deleteSnippet", HttpMethod.DELETE.name(), getSelfLink()));
            }
        }

        return operations.isEmpty() ? null : new ResourceMetaInfo(operations);
    }
}
